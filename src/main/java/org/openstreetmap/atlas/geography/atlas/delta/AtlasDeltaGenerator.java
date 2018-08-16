package org.openstreetmap.atlas.geography.atlas.delta;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * This CLI will allow you to point to two atlases and generate a diff of the two.
 *
 * You can point to single atlas files or directories of atlas shards. If you are diffing shards for both your input
 * and your output, we will process diffs for each shard individually in parallel.
 *
 * @author matthieun
 * @author hallahan
 */
public class AtlasDeltaGenerator extends Command
{
    private static final int THREADS = 8; // Tweak this if desired.

    private static final Switch<Path> BEFORE_SWITCH = new Switch<>("before",
            "The before atlas directory or file from which to delta.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> AFTER_SWITCH = new Switch<>("after",
            "The after atlas directory or file that the before atlas deltas to.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> OUTPUT_DIRECTORY_SWITCH = new Switch<>("outputDirectory",
            "The path of the output directory.", Paths::get, Optionality.REQUIRED);

    private static final PathMatcher ATLAS_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.atlas");

    private final Logger logger;


    public static void main(final String[] args)
    {
        new AtlasDeltaGenerator(LoggerFactory.getLogger(AtlasDeltaGenerator.class)).run(args);
    }

    public AtlasDeltaGenerator(final Logger logger)
    {
        this.logger = logger;
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Path before = (Path) command.get("before");
        final Path after = (Path) command.get("after");
        final Path outputDir = (Path) command.get("outputDir");
        run(before, after, outputDir);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(BEFORE_SWITCH, AFTER_SWITCH, OUTPUT_DIRECTORY_SWITCH);
    }

    private void run(final Path before, final Path after, final Path outputDir)
    {
        final Time time = Time.now();

        this.logger.info("Comparing {} and {}", before, after);

        // If the after is a directory, we want to diff the individual shards in parallel.
        if (Files.isDirectory(after))
        {
            // You need to have the before dir be a dir of shards too for this to work.
            if (!Files.isDirectory(before))
            {

                logger.error("Your -before parameter must point to a directory of atlas shards if you want to compare shard by shard with an -after directory also of shards!");
                System.exit(64);
            }

            // Execute in a pool of threads so we limit how many atlases get loaded in parallel.
            final ForkJoinPool customThreadPool = new ForkJoinPool(THREADS);
            try
            {
                customThreadPool.submit(() -> this.compareShardByShard(before, after)).get();
            }
            catch (final InterruptedException interrupt)
            {
                logger.error("The shard diff workers were interrupted.", interrupt);
            }
            catch (final ExecutionException exec)
            {
                logger.error("There was an execution exception on the shard diff workers.", exec);
            }
        }
        // Otherwise, we can do a normal compare where we look at 2 atlases or input shards with a single output.
        else
        {
            try
            {
                final Atlas beforeAtlas = load(before);
                final Atlas afterAtlas = load(after);
                compare(beforeAtlas, afterAtlas, outputDir);
            }
            catch (IOException ioex)
            {
                logger.error("Problem loading atlas files.", ioex);
            }
        }

        logger.info("AtlasDeltaGenerator complete. Total time: {}.", time.elapsedSince());
    }

    private Atlas load(final Path path) throws IOException {
        final Atlas atlas;

        // Make a MultiAtlas
        if (Files.isDirectory(path))
        {
            atlas = new AtlasResourceLoader().load(fetchAtlasFilesInDir(path));
        }
        // Just load the atlas
        else
        {
            atlas = new AtlasResourceLoader().load(new File(path.toFile()));
        }

        return atlas;
    }

    private void compareShardByShard(final Path before, final Path after)
    {
        try
        {
            fetchAtlasFilesInDir(after).parallelStream().forEach(afterFilePath ->
            {


            });
        }
        catch (IOException ioex)
        {
            logger.error("Problem fetching the atlas files in the after dir.", ioex);
        }
    }

    private void compare(final Atlas beforeAtlas, final Atlas afterAtlas, final Path outputDir)
    {
        final String name = FilenameUtils.removeExtension(beforeAtlas.getName());

        final AtlasDelta delta = new AtlasDelta(beforeAtlas, afterAtlas).generate();

        final String text = delta.toDiffViewFriendlyString();
        final File textFile = new File(
                outputDir.resolve(name + FileSuffix.TEXT.toString()).toFile());
        textFile.writeAndClose(text);
        this.logger.info("Saved txt file {}", textFile);

        final String geoJson = delta.toGeoJson();
        final File geoJsonFile = new File(
                outputDir.resolve(name + FileSuffix.GEO_JSON.toString()).toFile());
        geoJsonFile.writeAndClose(geoJson);
        this.logger.info("Saved GeoJSON file {}", geoJsonFile);

        final String relationsGeoJson = delta.toRelationsGeoJson();
        final String relationsGeoJsonFileName = name + "_relations"
                + FileSuffix.GEO_JSON.toString();
        final File relationsGeoJsonFile = new File(
                outputDir.resolve(relationsGeoJsonFileName).toFile());
        relationsGeoJsonFile.writeAndClose(relationsGeoJson);
        this.logger.info("Saved Relations GeoJSON file {}", relationsGeoJsonFile);
    }

    private static List<File> fetchAtlasFilesInDir(final Path dir) throws IOException
    {
        return Files.walk(dir).filter(Files::isRegularFile)
                .filter(ATLAS_MATCHER::matches)
                .map(path -> new File(path.toFile()))
                .collect(Collectors.toList());
    }
}

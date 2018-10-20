package org.openstreetmap.atlas.geography.atlas.geojson;

import static org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader.IS_ATLAS;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * This CLI takes a directory of atlas files and turns them into line-delimited GeoJSON.
 *
 * @author hallahan
 */
public class LineDelimitedGeoJsonConverter extends Command
{
    private static final int DEFAULT_THREADS = 8;

    /**
     * After all of your files are converted to LD GeoJSON, it is then concatenated into EVERYTHING.geojson
     */
    private static final String EVERYTHING = "EVERYTHING.geojson";

    private static final Logger logger = LoggerFactory.getLogger(LineDelimitedGeoJsonConverter.class);

    private static final AtlasResourceLoader ATLAS_RESOURCE_LOADER = new AtlasResourceLoader();

    private static final Switch<Path> ATLAS_DIRECTORY = new Switch<>("atlasDirectory",
            "The directory of atlases to convert.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> GEOJSON_DIRECTORY = new Switch<>("geojsonDirectory",
            "The directory to write line-delimited GeoJSON.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Boolean> OVERWRITE = new Switch<>("overwrite",
            "Choose to automatically overwrite a GeoJSON file if it exists at the given path.",
            Boolean::new, Optionality.OPTIONAL, "false");

    private static final Switch<Integer> THREADS = new Switch<>("threads",
            "The number of threads to work on processing atlas shards.", Integer::valueOf,
            Optionality.OPTIONAL, String.valueOf(DEFAULT_THREADS));

    /**
     * We only want positive edges, because the negative edge can be derived at the application
     * level, and this encodes extraneous data that can be easily derived by the map viewer. NOTE:
     * We're temporarily disabling RELATIONS right now here too!!!!
     */
    private static final Predicate<AtlasEntity> POSITIVE_ONLY = atlasEntity -> atlasEntity
            .getIdentifier() >= 0 && !ItemType.RELATION.equals(atlasEntity.getType());

    /**
     * If we are rendering vector tiles, we may want to examine various tags of a given atlas entity
     * and make decisions for the layer name, min zoom, and max zoom for the feature. Depending on your vector tile
     * renderer, as well as map data visualization needs, you can override this BiConsumer to mutate your
     * JSON object as you see fit.
     */
    protected static final BiConsumer<AtlasEntity, JsonObject> JSON_MUTATOR = (atlasEntity, feature) -> {};

    public static void main(final String[] args)
    {
        new LineDelimitedGeoJsonConverter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Time time = Time.now();
        final Path atlasDirectory = (Path) command.get(ATLAS_DIRECTORY);
        final Path geojsonDirectory = (Path) command.get(GEOJSON_DIRECTORY);
        final Boolean overwrite = (Boolean) command.get(OVERWRITE);
        final int threads = (Integer) command.get(THREADS);

        if (overwrite)
        {
            try
            {
                FileUtils.deleteDirectory(geojsonDirectory.toFile());
            }
            catch (final IOException noDelete)
            {
                logger.warn(
                        "Tried to delete GeoJSON output directory {} for overwrite, but unable.",
                        geojsonDirectory);
            }
        }

        final List<File> atlases = fetchAtlasFilesInDirectory(atlasDirectory);
        logger.info("About to convert {} atlas shards into line-delimited GeoJSON...",
                atlases.size());

        // Execute in a pool of threads so we limit how many atlases get loaded in parallel.
        final ForkJoinPool pool = new ForkJoinPool(threads);
        try
        {
            pool.submit(() -> this.convertAtlases(atlasDirectory, geojsonDirectory)).get();
             concatenate(geojsonDirectory);
        }
        catch (final InterruptedException interrupt)
        {
            logger.error("The atlas to GeoJSON workers were interrupted.", interrupt);
        }
        catch (final ExecutionException execution)
        {
            logger.error("There was an execution exception on the atlas to GeoJSON workers.",
                    execution);
        }
        finally
        {
            pool.shutdown();
        }

        logger.info(
                "Finished converting directory of atlas shards into line-delimited GeoJSON in {}!",
                time.elapsedSince());

        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(ATLAS_DIRECTORY, GEOJSON_DIRECTORY, OVERWRITE, THREADS);
    }

    private void convertAtlases(final Path atlasDirectory, final Path geojsonDirectory)
    {
        final List<File> atlases = fetchAtlasFilesInDirectory(atlasDirectory);
        atlases.parallelStream().forEach(atlasFile ->
        {
            final Time time = Time.now();
            final Atlas atlas = ATLAS_RESOURCE_LOADER.load(atlasFile);
            final String name = FilenameUtils.removeExtension(atlasFile.getName())
                    + FileSuffix.GEO_JSON.toString();
            final File geojsonFile = new File(geojsonDirectory.resolve(name).toFile());
            atlas.saveAsLineDelimitedGeoJsonFeatures(geojsonFile, POSITIVE_ONLY, JSON_MUTATOR);
            logger.info("Saved {} in {}.", name, time.elapsedSince());
        });
    }

    private void concatenate(final Path geojsonDirectory)
    {
        final Time time = Time.now();
        final String directory = geojsonDirectory.toString();

        // https://stackoverflow.com/questions/5080109/how-to-execute-bin-sh-with-commons-exec
        final String cat = String.format("cat '%s/'*.geojson > '%s/'%s", directory, directory, EVERYTHING);

        final CommandLine commandLine = CommandLine.parse("bash")
                .addArgument("-c", false)
                .addArgument(cat, false);

        logger.info("cmd: {}", commandLine.toString());

        final DefaultExecutor executor = new DefaultExecutor();
        try
        {
            executor.execute(commandLine);
            logger.info("Concatenated to {} in {}", EVERYTHING, time.elapsedSince());
        }
        catch (final IOException ioException)
        {
            logger.error("Unable to concatenate the output line-delimited GeoJSON files.",
                    ioException);
        }
    }

    private static List<File> fetchAtlasFilesInDirectory(final Path directory)
    {
        return new File(directory.toFile()).listFilesRecursively().stream().filter(IS_ATLAS)
                .collect(Collectors.toList());
    }
}

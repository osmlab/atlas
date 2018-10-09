package org.openstreetmap.atlas.geography.atlas.geojson;

import org.apache.commons.io.FileUtils;
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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader.IS_ATLAS;


/**
 * This CLI takes a directory of atlas files and turns them into GeoJSON, specifically to be consumed by tippecanoe to create MapboxVectorTiles.
 *
 * @author hallahan
 */
public class TippecanoeGeoJsonConverter extends Command
{
    private static final int DEFAULT_THREADS = 8;

    private static final Logger logger = LoggerFactory.getLogger(TippecanoeGeoJsonConverter.class);

    private static final AtlasResourceLoader ATLAS_RESOURCE_LOADER = new AtlasResourceLoader();

    private static final Switch<Path> ATLAS_DIRECTORY = new Switch<>("atlasDirectory",
            "The directory of atlases to convert.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> GEOJSON_DIRECTORY = new Switch<>("geojsonDirectory",
            "The directory to write tippecanoe GeoJSON.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Boolean> OVERWRITE = new Switch<>("overwrite",
            "Choose to automatically overwrite a GeoJSON file if it exists at the given path.",
            Boolean::new, Optionality.OPTIONAL, "false");

    private static final Switch<Integer> THREADS = new Switch<>("threads",
            "The number of threads to work on processing atlas shards.", Integer::valueOf,
            Optionality.OPTIONAL, String.valueOf(DEFAULT_THREADS));


    public static void main(final String[] args)
    {
        new TippecanoeGeoJsonConverter().run(args);
    }

    private static List<File> fetchAtlasFilesInDirectory(final Path directory)
    {
        return new File(directory.toFile()).listFilesRecursively().stream().filter(IS_ATLAS)
                .collect(Collectors.toList());
    }

    @Override
    protected int onRun(CommandMap command)
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
        logger.info("About to convert {} atlas shards into GeoJSON for tippecanoe...", atlases.size());

        // Execute in a pool of threads so we limit how many atlases get loaded in parallel.
        final ForkJoinPool pool = new ForkJoinPool(threads);
        try
        {
            pool.submit(() -> this.convertAtlases(atlasDirectory, geojsonDirectory)).get();
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

        logger.info("Finished converting directory of atlas shards into GeoJSON for tippecanoe in {}!", time.elapsedSince());
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
            final String name = FilenameUtils.removeExtension(atlasFile.getName()) + FileSuffix.GEO_JSON.toString();
            final File geojsonFile = new File(geojsonDirectory.resolve(name).toFile());
            atlas.saveAsGeoJson(geojsonFile);
            logger.info("Saved {} in {}.", name, time.elapsedSince());
        });
    }

}

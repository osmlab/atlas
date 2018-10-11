package org.openstreetmap.atlas.geography.atlas.geojson;

import static org.openstreetmap.atlas.geography.atlas.geojson.TippecanoeUtils.fetchAtlasFilesInDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This CLI takes a directory of atlas files and turns them into GeoJSON, specifically to be
 * consumed by tippecanoe to create MapboxVectorTiles.
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


    /**
     * We only want positive edges, because the negative edge can be derived at the application level, and this
     * encodes extraneous data that can be easily derived by the map viewer.
     */
    private static final Predicate<AtlasEntity> POSITIVE_ONLY = (atlasEntity -> atlasEntity.getIdentifier() >= 0);


    /**
     * For the render logic of tippecanoe, we want to examine various tags of a given atlas entity and make decisions
     * for the layer name, min zoom, and max zoom for the feature. These properties will be followed by tippecanoe
     * if you put it in a "tippecanoe" object within the JSON feature.
     */
    private static final BiConsumer<AtlasEntity, JsonObject> TIPPECANOEIFY = ((atlasEntity, feature) -> {
        final JsonObject tippecanoe = new JsonObject();

        final String atlasType = atlasEntity.getType().name();
        tippecanoe.addProperty("layer", atlasType);

        // things will have a min zoom of 10 by default
        int minzoom = 10;

        // lets do some more specific zooms
        final Map<String, String> tags = atlasEntity.getTags();

        final String highway = tags.get("highway");
        if (tags.get("waterway") != null || "motorway".equals(highway) )
        {
            minzoom = 6;
        }

        else if ("trunk".equals(highway) || "primary".equals(highway))
        {
            minzoom = 8;
        }

        else if ("secondary".equals(highway))
        {
            minzoom = 9;
        }

        else if ("NODE".equals(atlasType))
        {
            minzoom = 12;
        }

        tippecanoe.addProperty("minzoom", minzoom);
        feature.add("tippecanoe", tippecanoe);
    });


    public static void main(final String[] args)
    {
        new TippecanoeGeoJsonConverter().run(args);
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
        logger.info("About to convert {} atlas shards into GeoJSON for tippecanoe...",
                atlases.size());

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

        logger.info(
                "Finished converting directory of atlas shards into GeoJSON for tippecanoe in {}!",
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
            atlas.saveAsLineDelimitedGeoJson(geojsonFile, POSITIVE_ONLY, TIPPECANOEIFY);
            logger.info("Saved {} in {}.", name, time.elapsedSince());
        });
    }

}

package org.openstreetmap.atlas.vectortiles;

import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

import com.google.gson.JsonObject;

/**
 * This is where you configure the tippecanoe properties that will be in the generated
 * line-delimited GeoJSON. You also set the arguments to drive tippecanoe here.
 *
 * @author hallahan
 */
final class TippecanoeSettings
{
    private TippecanoeSettings()
    {
        // Utility Class
    }

    static final DefaultArtifactVersion MIN_VERSION = new DefaultArtifactVersion("1.32.1");
    static final String GEOJSON = "EVERYTHING.geojson";

    static final String[] ARGS = new String[] { "-Z7", "-z14", "--generate-ids", "--read-parallel",
            "--no-tile-size-limit", "--no-feature-limit" };

    static final BiConsumer<AtlasEntity, JsonObject> JSON_MUTATOR = (atlasEntity, feature) ->
    {
        final JsonObject tippecanoe = new JsonObject();

        final String atlasType = atlasEntity.getType().name();
        tippecanoe.addProperty("layer", atlasType);

        // things will have a min zoom of 11 by default
        int minzoom = 11;

        // lets do some more specific zooms
        final Map<String, String> tags = atlasEntity.getTags();

        final String highway = tags.get("highway");

        if (tags.get("boundary") != null)
        {
            minzoom = 7;
        }
        else if (tags.get("waterway") != null || "motorway".equals(highway))
        {
            minzoom = 8;
        }

        else if ("trunk".equals(highway) || "primary".equals(highway))
        {
            minzoom = 9;
        }

        else if ("secondary".equals(highway))
        {
            minzoom = 10;
        }

        else if ("NODE".equals(atlasType))
        {
            minzoom = 12;
        }

        tippecanoe.addProperty("minzoom", minzoom);
        feature.add("tippecanoe", tippecanoe);
    };
}

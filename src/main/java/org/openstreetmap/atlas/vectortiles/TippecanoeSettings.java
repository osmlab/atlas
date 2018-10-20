package org.openstreetmap.atlas.vectortiles;

import com.google.gson.JsonObject;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

import java.util.Map;
import java.util.function.BiConsumer;

class TippecanoeSettings
{
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

    static final String[] ARGS = new String[] {
            "-Z7",
            "-z14",
            "--read-parallel",
            "--no-tile-size-limit",
            "--no-feature-limit"
    };
}

package org.openstreetmap.atlas.utilities.vectortiles;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This singleton class is where you get the minimum zoom for features' tags based on a JSON
 * configuration. The values you see in the minimum-zoom.json resourse file is inspired for what you
 * see in the standard OpenStreetMap carto style. This is very loosely based off of the minimum
 * zooms you see for various types of features. Note that there is definitely more work that needs
 * to be done to refine our min zooms.
 *
 * https://github.com/gravitystorm/openstreetmap-carto
 *
 * The config is a JSON array of rule objects. Each rule looks like this:
 *
 *   {
 *     "key": "landuse",
 *     "default": 12,
 *     "values": {
 *       "basin": 7,
 *       "forest": 8
 *     }
 *   }
 *
 * The rule must have a key for the tag key. It must have an integer for the default minimum zoom.
 * values is optional, and this is an object with a given OSM tag value and a minimum zoom that will
 * apply to it. The way that the JSON config evaluates is that the first rules in the array take
 * priority. If a given key matches, we use that rule, and all other rules will not be evaluated for
 * finding the minimum zoom for that given atlas element's tags.
 *
 *
 * @author hallahan
 */
enum MinimumZoom
{
    INSTANCE;

    private static final String CONFIG_RESOURCE = "minimum-zooms.json";
    private static final int DEFAULT_ZOOM = 14;

    private String[] keys;
    private int[] defaults;
    private List<Map<String, Integer>> valuesList = new ArrayList<>();

    MinimumZoom()
    {
        processConfig(parseConfig());
    }

    private JsonArray parseConfig()
    {
        final InputStream inputStream = MinimumZoom.class.getResourceAsStream(CONFIG_RESOURCE);
        final InputStreamReader reader = new InputStreamReader(inputStream);
        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(reader);
        return element.getAsJsonArray();
    }

    private void processConfig(final JsonArray config)
    {
        final int length = config.size();
        keys = new String[length];
        defaults = new int[length];
        for (int index = 0; index < length; ++index)
        {
            final JsonObject object = config.get(index).getAsJsonObject();
            keys[index] = object.get("key").getAsString();
            defaults[index] = object.get("default").getAsInt();

            final Map<String, Integer> valuesMap = new HashMap<>();
            valuesList.add(index, valuesMap);

            // values is optional
            final JsonElement valuesElement = object.get("values");
            if (valuesElement != null)
            {
                valuesElement.getAsJsonObject().entrySet().forEach(
                        entry -> valuesMap.put(entry.getKey(), entry.getValue().getAsInt()));
            }
        }
    }

    int get(final Map<String, String> tags)
    {
        for (int index = 0; index < keys.length; ++index)
        {
            final String key = keys[index];
            final String value = tags.get(key);
            if (value == null)
            {
                continue;
            }
            final Map<String, Integer> valuesMap = valuesList.get(index);
            final Integer valueMinimumZoom = valuesMap.get(value);
            if (valueMinimumZoom != null)
            {
                return valueMinimumZoom;
            }
            else
            {
                return defaults[index];
            }
        }
        return DEFAULT_ZOOM;
    }
}

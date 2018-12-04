package org.openstreetmap.atlas.utilities.vectortiles;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This singleton class is where you get the minimum zoom for features' tags based on a JSON
 * configuration. The values you see in the minimum-zoom.json resource file is inspired for what you
 * see in the standard OpenStreetMap carto style. This is very loosely based off of the minimum
 * zooms you see for various types of features. See the README.md in this package for more
 * details...
 *
 * @author hallahan
 */
public enum MinimumZoom
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
        try
        {
            final InputStream inputStream = MinimumZoom.class.getResourceAsStream(CONFIG_RESOURCE);
            final InputStreamReader reader = new InputStreamReader(inputStream);
            final JsonParser parser = new JsonParser();
            final JsonElement element = parser.parse(reader);
            return element.getAsJsonArray();
        }
        catch (final Exception exception)
        {
            throw new CoreException(
                    "There was a problem parsing minimum-zooms.json. Check if the JSON file has valid structure.",
                    exception);
        }
    }

    private void processConfig(final JsonArray config)
    {
        final int length = config.size();
        keys = new String[length];
        defaults = new int[length];
        for (int index = 0; index < length; ++index)
        {
            try
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
            catch (final Exception exception)
            {
                throw new CoreException(
                        "There is a problem with one of the rule objects in the JSON configuration.",
                        exception);
            }
        }
    }

    public int get(final Map<String, String> tags)
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

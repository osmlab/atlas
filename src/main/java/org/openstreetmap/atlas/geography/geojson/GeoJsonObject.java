package org.openstreetmap.atlas.geography.geojson;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A Geo {@link JsonObject} with properties.
 *
 * @author matthieun
 */
public class GeoJsonObject
{
    private JsonObject jsonObject;

    protected GeoJsonObject(final JsonObject jsonObject)
    {
        this.jsonObject = jsonObject;
    }

    public Map<String, Object> getProperties()
    {
        final Map<String, Object> result = new HashMap<>();
        if (this.jsonObject.get("properties") != null)
        {
            final JsonObject propertiesObject = (JsonObject) this.jsonObject.get("properties");
            for (final Map.Entry<String, JsonElement> entry : propertiesObject.entrySet())
            {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return result;
    }

    public JsonObject jsonObject()
    {
        return this.jsonObject;
    }

    public void makeFeatureCollection()
    {
        if (!"FeatureCollection".equals(this.jsonObject.get("type").getAsString()))
        {
            final JsonObject result = new JsonObject();
            result.addProperty("type", "FeatureCollection");
            final JsonArray features = new JsonArray();
            features.add(this.jsonObject);
            result.add("features", features);
            this.jsonObject = result;
        }
    }

    public void save(final WritableResource output)
    {
        final BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(output.write(), StandardCharsets.UTF_8));
        try
        {
            writer.write(this.jsonObject.toString());
            Streams.close(writer);
        }
        catch (final Exception e)
        {
            Streams.close(writer);
            throw new CoreException("Could not save geojson object", e);
        }
    }

    public String toPrettyString()
    {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this.jsonObject);
    }

    @Override
    public String toString()
    {
        return this.jsonObject.toString();
    }

    public GeoJsonObject withNewProperties(final Map<String, ? extends Object> properties)
    {
        final JsonObject propertiesObject;
        if (this.jsonObject.get("properties") != null)
        {
            propertiesObject = (JsonObject) this.jsonObject.get("properties");
            this.jsonObject.remove("properties");
        }
        else
        {
            propertiesObject = new JsonObject();
        }
        final Gson gson = new Gson();
        properties.forEach((key, value) -> propertiesObject.add(key, gson.toJsonTree(value)));
        this.jsonObject.add("properties", propertiesObject);
        return this;
    }

    public GeoJsonObject withNewProperty(final String key, final Object value)
    {
        final Map<String, Object> properties = new HashMap<>();
        properties.put(key, value);
        return this.withNewProperties(properties);
    }
}

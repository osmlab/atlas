package org.openstreetmap.atlas.streaming.readers.json.serializers;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.streaming.readers.json.converters.PolyLineCoordinateConverter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @param <T>
 *            The type of {@link PolyLine} to serialize.
 * @author matthieun
 */
public abstract class MultiLocationSerializer<T extends PolyLine> implements JsonSerializer<T>
{
    @Override
    public JsonElement serialize(final T polyLine, final Type typeOfSrc,
            final JsonSerializationContext context)
    {
        final JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive(getType()));
        polyLine.forEach(location ->
        {
        });
        result.add("coordinates", new PolyLineCoordinateConverter().convert(polyLine));
        return result;
    }

    /**
     * @return The type of multi-location item
     */
    protected abstract String getType();
}

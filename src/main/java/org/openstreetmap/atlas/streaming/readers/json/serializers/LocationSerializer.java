package org.openstreetmap.atlas.streaming.readers.json.serializers;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.streaming.readers.json.converters.PointCoordinateConverter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * {@link JsonSerializer} for a {@link Location}
 *
 * @author matthieun
 */
public class LocationSerializer implements JsonSerializer<Location>
{
    @Override
    public JsonElement serialize(final Location location, final Type typeOfSrc,
            final JsonSerializationContext context)
    {
        final JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive("Point"));
        result.add("coordinates", new PointCoordinateConverter().convert(location));
        return result;
    }
}

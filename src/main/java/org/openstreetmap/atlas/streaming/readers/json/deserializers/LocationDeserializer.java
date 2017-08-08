package org.openstreetmap.atlas.streaming.readers.json.deserializers;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.streaming.readers.json.converters.PointCoordinateConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author matthieun
 */
public class LocationDeserializer implements JsonDeserializer<Location>
{
    private final PointCoordinateConverter coordinateConverter = new PointCoordinateConverter();

    @Override
    public Location deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException
    {
        final JsonArray coordinates = (JsonArray) ((JsonObject) json).get("coordinates");
        return this.coordinateConverter.revert().convert(coordinates);
    }
}

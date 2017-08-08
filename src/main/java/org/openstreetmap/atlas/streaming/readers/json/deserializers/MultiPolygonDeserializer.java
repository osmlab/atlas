package org.openstreetmap.atlas.streaming.readers.json.deserializers;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.streaming.readers.json.converters.MultiPolygonCoordinateConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author brian_l_davis
 */
public class MultiPolygonDeserializer implements JsonDeserializer<MultiPolygon>
{
    private final MultiPolygonCoordinateConverter multiMultiCoordinateConverter = new MultiPolygonCoordinateConverter();

    @Override
    public MultiPolygon deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException
    {
        final JsonArray coordinates = (JsonArray) ((JsonObject) json).get("coordinates");
        return this.multiMultiCoordinateConverter.revert().convert(coordinates);
    }
}

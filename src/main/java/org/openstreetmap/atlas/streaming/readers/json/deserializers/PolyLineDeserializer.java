package org.openstreetmap.atlas.streaming.readers.json.deserializers;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.streaming.readers.json.converters.PolyLineCoordinateConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author matthieun
 */
public class PolyLineDeserializer implements JsonDeserializer<PolyLine>
{
    private final PolyLineCoordinateConverter multiCoordinateConverter = new PolyLineCoordinateConverter();

    @Override
    public PolyLine deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException
    {
        final JsonArray coordinates = (JsonArray) ((JsonObject) json).get("coordinates");
        return new PolyLine(this.multiCoordinateConverter.revert().convert(coordinates));
    }
}

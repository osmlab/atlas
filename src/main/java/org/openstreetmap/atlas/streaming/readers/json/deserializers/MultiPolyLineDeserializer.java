package org.openstreetmap.atlas.streaming.readers.json.deserializers;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.geography.MultiPolyLine;
import org.openstreetmap.atlas.streaming.readers.json.converters.MultiPolyLineCoordinateConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author chunzhu
 */
public class MultiPolyLineDeserializer implements JsonDeserializer<MultiPolyLine>
{
    private final MultiPolyLineCoordinateConverter multiMultiCoordinateConverter = new MultiPolyLineCoordinateConverter();

    @Override
    public MultiPolyLine deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException
    {
        final JsonArray coordinates = (JsonArray) ((JsonObject) json).get("coordinates");
        return this.multiMultiCoordinateConverter.revert().convert(coordinates);
    }
}

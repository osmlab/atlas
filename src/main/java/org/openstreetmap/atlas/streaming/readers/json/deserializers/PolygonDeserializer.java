package org.openstreetmap.atlas.streaming.readers.json.deserializers;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.streaming.readers.json.converters.PolygonCoordinateConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author matthieun
 */
public class PolygonDeserializer implements JsonDeserializer<Polygon>
{
    private final PolygonCoordinateConverter multiMultiCoordinateConverter = new PolygonCoordinateConverter();

    /**
     * @deprecated Currently doesn't return accurate geometric representations for Geojson polygons
     *             with inner rings use {@link MultiPolygonDeserializer} and check to see if the
     *             returned multipolygon is a simple polygon
     **/
    @Deprecated
    @Override
    public Polygon deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException
    {
        final JsonArray coordinates = (JsonArray) ((JsonObject) json).get("coordinates");
        return new Polygon(this.multiMultiCoordinateConverter.revert().convert(coordinates));
    }
}

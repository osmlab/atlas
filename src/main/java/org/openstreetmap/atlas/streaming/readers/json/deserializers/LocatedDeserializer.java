package org.openstreetmap.atlas.streaming.readers.json.deserializers;

import java.lang.reflect.Type;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.GeoJsonType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializer that is clever about re-directing to the proper Located type.
 *
 * @author matthieun
 */
public class LocatedDeserializer implements JsonDeserializer<Located>
{
    @Override
    public Located deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException
    {
        final GeoJsonType type = GeoJsonType.forType(((JsonObject) json).get("type").getAsString());
        if (GeoJsonType.POINT == type)
        {
            return new LocationDeserializer().deserialize(json, typeOfT, context);
        }
        else if (GeoJsonType.LINESTRING == type)
        {
            return new PolyLineDeserializer().deserialize(json, typeOfT, context);
        }
        else if (GeoJsonType.POLYGON == type)
        {
            return new PolygonDeserializer().deserialize(json, typeOfT, context);
        }
        else if (GeoJsonType.MULTI_POLYGON == type)
        {
            return new MultiPolygonDeserializer().deserialize(json, typeOfT, context);
        }
        throw new CoreException("Unknown/unsupported geometry type: " + type);
    }
}

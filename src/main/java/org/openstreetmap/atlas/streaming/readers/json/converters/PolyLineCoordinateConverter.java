package org.openstreetmap.atlas.streaming.readers.json.converters;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.conversion.Converter;

import com.google.gson.JsonArray;

/**
 * Used for {@link PolyLine}s
 *
 * @author matthieun
 */
public class PolyLineCoordinateConverter implements Converter<Iterable<Location>, JsonArray>
{
    private final PointCoordinateConverter coordinateConverter = new PointCoordinateConverter();

    @Override
    public JsonArray convert(final Iterable<Location> object)
    {
        final JsonArray result = new JsonArray();
        object.forEach(location -> result.add(this.coordinateConverter.convert(location)));
        return result;
    }

    public Converter<JsonArray, List<Location>> revert()
    {
        return jsonArray ->
        {
            final List<Location> result = new ArrayList<>();
            jsonArray.forEach(jsonElement ->
            {
                final JsonArray array = (JsonArray) jsonElement;
                result.add(this.coordinateConverter.revert().convert(array));
            });
            return result;
        };
    }
}

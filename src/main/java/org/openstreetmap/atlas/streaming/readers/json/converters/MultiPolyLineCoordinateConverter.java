package org.openstreetmap.atlas.streaming.readers.json.converters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolyLine;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.conversion.Converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Used for {@link MultiPolyLine}s
 * 
 * @author chunzhu
 */
public class MultiPolyLineCoordinateConverter implements Converter<MultiPolyLine, JsonArray>
{
    private final PointCoordinateConverter coordinateConverter = new PointCoordinateConverter();

    @Override
    public JsonArray convert(final MultiPolyLine object)
    {
        final JsonArray result = new JsonArray();

        object.forEach(polyline ->
        {
            final JsonArray locations = new JsonArray();
            polyline.forEach(location -> locations.add(this.coordinateConverter.convert(location)));
            result.add(locations);
        });
        return result;
    }

    public Converter<JsonArray, MultiPolyLine> revert()
    {
        return jsonArray ->
        {
            final List<PolyLine> polyLines = new ArrayList<>();
            jsonArray.forEach(polyline ->
            {
                final JsonArray points = (JsonArray) polyline;
                final List<Location> locations = new ArrayList<>();
                final Iterator<JsonElement> locationIterator = points.iterator();
                while (locationIterator.hasNext())
                {
                    final JsonArray coordinate = (JsonArray) locationIterator.next();
                    locations.add(this.coordinateConverter.revert().convert(coordinate));
                }
                final PolyLine convertedPolyLine = new PolyLine(locations);
                polyLines.add(convertedPolyLine);
            });
            if (polyLines.isEmpty())
            {
                throw new CoreException("Cannot have an empty polyLines.");
            }
            return new MultiPolyLine(polyLines);
        };
    }
}

package org.openstreetmap.atlas.streaming.readers.json.converters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.conversion.Converter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Used for MultiPolygons
 *
 * @author brian_l_davis
 */
public class MultiPolygonCoordinateConverter implements Converter<MultiPolygon, JsonArray>
{
    private final PointCoordinateConverter coordinateConverter = new PointCoordinateConverter();

    @Override
    public JsonArray convert(final MultiPolygon object)
    {
        final JsonArray result = new JsonArray();

        object.outers().forEach(outerPolygon ->
        {
            final JsonArray outerLocations = new JsonArray();
            outerPolygon.forEach(
                    location -> outerLocations.add(this.coordinateConverter.convert(location)));
            result.add(outerLocations);
            object.innersOf(outerPolygon).forEach(innerPolygon ->
            {
                final JsonArray innerLocations = new JsonArray();
                innerPolygon.forEach(
                        location -> innerLocations.add(this.coordinateConverter.convert(location)));
                result.add(innerLocations);
            });
        });
        return result;
    }

    public Converter<JsonArray, MultiPolygon> revert()
    {
        return jsonArray ->
        {
            final MultiMap<Polygon, Polygon> outerToInners = new MultiMap<>();
            jsonArray.forEach(polygon ->
            {

                final JsonArray linearRings = (JsonArray) polygon;
                final Iterator<JsonElement> linearRingsIterator = linearRings.iterator();
                if (linearRingsIterator.hasNext())
                {

                    final List<Polygon> polygons = new ArrayList<>();
                    while (linearRingsIterator.hasNext())
                    {
                        final JsonArray coordinates = (JsonArray) linearRingsIterator.next();
                        final List<Location> locations = new ArrayList<>();
                        coordinates.forEach(coordinate ->
                        {
                            final JsonArray points = (JsonArray) coordinate;
                            locations.add(this.coordinateConverter.revert().convert(points));
                        });
                        if (locations.isEmpty()
                                || !locations.get(0).equals(locations.get(locations.size() - 1)))
                        {
                            throw new CoreException(
                                    "Invalidly formatted Geojson Polygon within Multipolygon");
                        }
                        // in valid geojson the first point is repeated at the end and does not need
                        // to be for our polygons
                        locations.remove(locations.size() - 1);
                        polygons.add(new Polygon(locations));
                    }
                    if (polygons.isEmpty())
                    {
                        throw new CoreException("Cannot have an empty MultiPolygon.");
                    }
                    final Polygon outer = polygons.remove(0);
                    outerToInners.put(outer, new ArrayList<>());
                    polygons.forEach(inner ->
                    {
                        if (!outer.fullyGeometricallyEncloses(inner))
                        {
                            throw new CoreException(
                                    "MultiPolygon inner ring not enclosed by outer ring.");
                        }
                        outerToInners.add(outer, inner);
                    });
                }
            });
            if (outerToInners.isEmpty())
            {
                throw new CoreException("Cannot have an empty MultiPolygon.");
            }
            return new MultiPolygon(outerToInners);
        };
    }
}

package org.openstreetmap.atlas.geography.geojson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;

public class GeoJsonUtils {

    private GeoJsonUtils()
    {
        // Utility class.
    }

    public static JsonArray locationsToCoordinates(final Iterable<Location> locations)
    {
        final JsonArray coordinates = new JsonArray();
        for (final Location point : locations)
        {
            coordinates.add(coordinate(point));
        }
        return coordinates;
    }

    public static JsonObject boundsToPolygonGeometry(final Rectangle bounds)
    {
        final JsonObject geometry = new JsonObject();

        final Location lowerLeft = bounds.lowerLeft();
        final Location upperRight = bounds.upperRight();
        final double minLon = lowerLeft.getLongitude().asDegrees();
        final double minLat = lowerLeft.getLatitude().asDegrees();
        final double maxLon = upperRight.getLongitude().asDegrees();
        final double maxLat = upperRight.getLatitude().asDegrees();

        final JsonArray outerRing = new JsonArray();
        outerRing.add(coordinate(minLon, minLat));
        outerRing.add(coordinate(minLon, maxLat));
        outerRing.add(coordinate(maxLon, maxLat));
        outerRing.add(coordinate(maxLon, minLat));
        outerRing.add(coordinate(minLon, minLat));

        final JsonArray coordinates = new JsonArray();
        coordinates.add(outerRing);

        geometry.addProperty("type", "Polygon");
        geometry.add("coordinates", coordinates);

        return geometry;
    }

    public static JsonArray coordinate(final Location location)
    {
        return coordinate(location.getLongitude().asDegrees(), location.getLatitude().asDegrees());
    }

    public static JsonArray coordinate(final double longitude, final double latitude)
    {
        final JsonArray coordinate = new JsonArray();
        coordinate.add(new JsonPrimitive(latitude));
        coordinate.add(new JsonPrimitive(longitude));
        return coordinate;
    }
}

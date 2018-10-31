package org.openstreetmap.atlas.geography.geojson;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * These are utility functions that well help you create GeoJSON!
 *
 * @author hallahan
 */
public final class GeoJsonUtils
{

    private GeoJsonUtils()
    {
        // Utility class.
    }

    /**
     * An iterable of locations will turn into a JsonArray of Longitude, Latitude coordinates.
     *
     * @param locations
     *            An iterable of locations
     * @return A JsonArray of Longitude, Latitude coordinates.
     */
    public static JsonArray locationsToCoordinates(final Iterable<Location> locations)
    {
        final JsonArray coordinates = new JsonArray();
        for (final Location point : locations)
        {
            coordinates.add(coordinate(point));
        }
        return coordinates;
    }

    /**
     * Creates a GeoJSON Polygon geometry from a bounds.
     *
     * @param bounds
     *            A bounds.
     * @return A GeoJSON Polygon geometry JsonObject.
     */
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

    /**
     * From a location, we get a Latitude / Longitude Json Array [ latitude, longitude ]
     *
     * @param location
     *            An atlas location
     * @return a JsonArray with the first coordinate as a latitude and the second as a longitude.
     */
    public static JsonArray coordinate(final Location location)
    {
        return coordinate(location.getLongitude().asDegrees(), location.getLatitude().asDegrees());
    }

    public static JsonArray coordinate(final double longitude, final double latitude)
    {
        final JsonArray coordinate = new JsonArray();
        coordinate.add(new JsonPrimitive(longitude));
        coordinate.add(new JsonPrimitive(latitude));
        return coordinate;
    }
}

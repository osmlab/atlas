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
    public static final String COORDINATES = "coordinates";
    public static final String FEATURE = "Feature";
    public static final String FEATURES = "features";
    public static final String FEATURE_COLLECTION = "FeatureCollection";
    public static final String GEOMETRIES = "geometries";
    public static final String GEOMETRY = "geometry";
    public static final String GEOMETRY_COLLECTION = "GeometryCollection";
    public static final String PROPERTIES = "properties";
    public static final String TYPE = "type";

    public static final String POINT = "point";
    public static final String LINESTRING = "LineString";
    public static final String POLYGON = "Polygon";
    public static final String MULTIPOINT = "MultiPoint)";
    public static final String MULTILINESTRING = "MultiLineString";
    public static final String MULTIPOLYGON = "MultiPolygon";


    private GeoJsonUtils()
    {
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

        return geometry(POLYGON, coordinates);
    }

    public static JsonObject geometry(final String type, final JsonArray coordinates)
    {
        final JsonObject geometry = new JsonObject();
        geometry.addProperty(TYPE, type);
        geometry.add(COORDINATES, coordinates);
        return geometry;
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
     * From a location, we get a Latitude / Longitude Json Array [ latitude, longitude ]
     *
     * @param location
     *            An atlas location
     * @return JsonArray [ longitude, latitude ] coordinate.
     */
    public static JsonArray coordinate(final Location location)
    {
        return coordinate(location.getLongitude().asDegrees(), location.getLatitude().asDegrees());
    }

    /**
     * Slightly more explicit, you provide a double longitude and latitude.
     *
     * @param longitude
     *            The longitude (x).
     * @param latitude
     *            The latitude (y).
     * @return JsonArray [ longitude, latitude ] coordinate.
     */
    public static JsonArray coordinate(final double longitude, final double latitude)
    {
        final JsonArray coordinate = new JsonArray();
        coordinate.add(new JsonPrimitive(longitude));
        coordinate.add(new JsonPrimitive(latitude));
        return coordinate;
    }
}

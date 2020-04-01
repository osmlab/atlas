package org.openstreetmap.atlas.geography.geojson;

import java.util.EnumSet;

import org.openstreetmap.atlas.exception.CoreException;

import com.google.gson.JsonObject;

/**
 * Geojson types case sensitive from the specification definition section 1.4:
 * https://tools.ietf.org/html/rfc7946#section-1.4
 * 
 * <pre>
 *o  Inside this document, the term "geometry type" refers to seven
 *       case-sensitive strings: "Point", "MultiPoint", "LineString",
 *       "MultiLineString", "Polygon", "MultiPolygon", and
 *       "GeometryCollection".
 *o  As another shorthand notation, the term "GeoJSON types" refers to
 *       nine case-sensitive strings: "Feature", "FeatureCollection", and
 *       the geometry types listed above.
 * </pre>
 *
 * The reasoning for this being an enum also comes from the spec section 7:
 * https://tools.ietf.org/html/rfc7946#section-7
 * 
 * <pre>
 *      Implementations MUST NOT extend the fixed set of GeoJSON types:
 *    FeatureCollection, Feature, Point, LineString, MultiPoint, Polygon,
 *    MultiLineString, MultiPolygon, and GeometryCollection.
 * </pre>
 *
 * @author matthieun
 * @author mgostintsev
 * @author jklamer
 */
public enum GeoJsonType
{
    FEATURE("Feature"),
    FEATURE_COLLECTION("FeatureCollection"),
    POINT("Point"),
    MULTI_POINT("MultiPoint"),
    LINESTRING("LineString"),
    MULTI_LINESTRING("MultiLineString"),
    POLYGON("Polygon"),
    MULTI_POLYGON("MultiPolygon"),
    GEOMETRY_COLLECTION("GeometryCollection");

    private static final EnumSet GEOMETRY_TYPES = EnumSet.of(POINT, MULTI_POINT, LINESTRING,
            MULTI_LINESTRING, POLYGON, MULTI_POLYGON, GEOMETRY_COLLECTION);
    private static final EnumSet FEATURE_TYPES = EnumSet.of(FEATURE, FEATURE_COLLECTION);
    private final String typeString;

    public static GeoJsonType forJson(final JsonObject object)
    {
        final String typeString;
        try
        {
            typeString = object.get(GeoJsonConstants.TYPE).getAsString();
        }
        catch (final Exception exception)
        {
            throw new CoreException("Invalid geoJson type: {}", object.get(GeoJsonConstants.TYPE));
        }
        return forString(typeString);
    }

    public static GeoJsonType forString(final String type)
    {
        for (final GeoJsonType value : values())
        {
            if (value.getTypeString().equals(type))
            {
                return value;
            }
        }
        throw new CoreException("Invalid geoJson type: {}", type);
    }

    public static boolean isFeatureType(final GeoJsonType type)
    {
        return FEATURE_TYPES.contains(type);
    }

    public static boolean isGeometryType(final GeoJsonType type)
    {
        return GEOMETRY_TYPES.contains(type);
    }

    GeoJsonType(final String typeString)
    {
        this.typeString = typeString;
    }

    public String getTypeString()
    {
        return this.typeString;
    }

    @Override
    public String toString()
    {
        return this.typeString;
    }
}

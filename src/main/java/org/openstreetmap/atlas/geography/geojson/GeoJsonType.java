package org.openstreetmap.atlas.geography.geojson;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 * @author mgostintsev
 */
public enum GeoJsonType
{
    POINT("Point"),
    LINESTRING("LineString"),
    POLYGON("Polygon"),
    MULTI_POINT("MultiPoint"),
    MULTI_LINESTRING("MultiLineString"),
    MULTI_POLYGON("MultiPolygon");

    private final String type;

    public static GeoJsonType forType(final String type)
    {
        for (final GeoJsonType value : values())
        {
            if (value.getType().equals(type))
            {
                return value;
            }
        }
        throw new CoreException("Invalid geoJson type: {}", type);
    }

    GeoJsonType(final String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }
}

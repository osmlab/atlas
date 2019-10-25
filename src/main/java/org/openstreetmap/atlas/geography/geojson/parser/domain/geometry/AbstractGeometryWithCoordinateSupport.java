package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.Map;

/**
 * An abstraction of geometries with coordinates.
 *
 * @param <C>
 *            - coordinate data type.
 * @author Yazad Khambata
 */
public abstract class AbstractGeometryWithCoordinateSupport<C> extends AbstractGeometry
        implements GeometryWithCoordinates<C>
{
    // TODO: private Properties properties;

    public AbstractGeometryWithCoordinateSupport(final Map<String, Object> map)
    {
        super(map);
    }

    public static Object extractRawCoordinates(final Map<String, Object> map)
    {
        return map.get("coordinates");
    }
}

package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.DefaultForeignFieldsImpl;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;

/**
 * An abstraction of geometries with coordinates.
 *
 * @param <C>
 *            - coordinate data type.
 * @param <G>
 *            - Atlas Geometry.
 * @author Yazad Khambata
 */
public abstract class AbstractGeometryWithCoordinateSupport<C, G> extends AbstractGeometry
        implements GeometryWithCoordinates<C, G>
{
    public static Object extractRawCoordinates(final Map<String, Object> map)
    {
        return map.get("coordinates");
    }

    public AbstractGeometryWithCoordinateSupport(final Map<String, Object> map)
    {
        super(map, new DefaultForeignFieldsImpl(extractForeignFields(map,
                new HashSet<>(Arrays.asList("type", "bbox", "coordinates", "properties")))));
    }

    public AbstractGeometryWithCoordinateSupport(final Map<String, Object> map,
            final ForeignFields foreignFields)
    {
        super(map, foreignFields);
    }
}

package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.DefaultForeignFieldsImpl;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * An abstraction of geometries with coordinates.
 *
 * @param <C> - coordinate data type.
 * @author Yazad Khambata
 */
public abstract class AbstractGeometryWithCoordinateSupport<C> extends AbstractGeometry implements GeometryWithCoordinates<C> {

    public AbstractGeometryWithCoordinateSupport(final Map<String, Object> map) {
        super(map, new DefaultForeignFieldsImpl(extractForeignFields(map, new HashSet<>(Arrays.asList("type", "bbox",
                "coordinates", "properties")))));
    }

    public AbstractGeometryWithCoordinateSupport(final Map<String, Object> map, final ForeignFields foreignFields) {
        super(map, foreignFields);
    }

    public static Object extractRawCoordinates(final Map<String, Object> map) {
        return map.get("coordinates");
    }
}

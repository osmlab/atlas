package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;

/**
 * @author Yazad Khambata
 */
public class MultiPolygon extends AbstractGeometry implements GeometryWithCoordinateSupport<List<List<Position>>> {
    private MultiLineString value;

    @Override
    public Coordinates<List<List<Position>>> getCoordinates() {
        return value.getCoordinates();
    }
}

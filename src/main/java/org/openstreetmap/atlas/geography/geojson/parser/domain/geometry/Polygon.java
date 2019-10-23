package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;

/**
 * @author Yazad Khambata
 */
public class Polygon extends AbstractGeometry implements GeometryWithCoordinateSupport<List<Position>> {
    private LineString value;

    public Coordinates<List<Position>> getCoordinates() {
        return value.getCoordinates();
    }
}

package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;

/**
 * @author Yazad Khambata
 */
public class MultiLineString extends AbstractGeometry implements GeometryWithCoordinateSupport<List<List<Position>>> {
    private Coordinates<List<List<Position>>> coordinates; //zero or more but each sub array must contain at least 2 positions.

    @Override
    public Coordinates<List<List<Position>>> getCoordinates() {
        return coordinates;
    }
}

package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;

import java.util.List;
import java.util.Map;

/**
 * @author Yazad Khambata
 */
public class Point extends AbstractGeometryWithCoordinateSupport<Position> {
    private Coordinates<Position> coordinates;

    public Point(final Map<String, Object> map) {
        super(map);
        this.coordinates = Coordinates.forPoint((List<Double>) extractRawCoordinates(map));
    }

    @Override
    public Coordinates<Position> getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        return "Point{" +
                "coordinates=" + coordinates +
                "} " + super.toString();
    }
}

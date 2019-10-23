package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

/**
 * @author Yazad Khambata
 */
public class Point extends AbstractGeometry implements GeometryWithCoordinateSupport<Position> {
    private Coordinates<Position> coordinates;

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

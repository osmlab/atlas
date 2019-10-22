package org.openstreetmap.atlas.geography.geojson.concrete.geometry;

import org.openstreetmap.atlas.geography.geojson.concrete.AbstractGeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.concrete.type.GeometryType;

/**
 * @author Yazad Khambata
 */
public class Point extends AbstractGeometry implements GeometryWithCoordinateSupport<Position> {
    private Coordinates<Position> coordinates;

    @Override
    public Coordinates<Position> getCoordinates() {
        return coordinates;
    }
}

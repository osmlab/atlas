package org.openstreetmap.atlas.geography.geojson.concrete.geometry;

import org.openstreetmap.atlas.geography.geojson.concrete.AbstractGeoJsonItem;

import java.util.List;

/**
 * @author Yazad Khambata
 */
public class LineString extends AbstractGeometry implements GeometryWithCoordinateSupport<List<Position>> {
    private MultiPoint value; //Must have at least 2 positions.

    @Override
    public Coordinates<List<Position>> getCoordinates() {
        return value.getCoordinates();
    }
}

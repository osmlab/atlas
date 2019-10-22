package org.openstreetmap.atlas.geography.geojson.concrete.geometry;

import org.openstreetmap.atlas.geography.geojson.concrete.AbstractGeoJsonItem;

import java.util.List;

/**
 * @author Yazad Khambata
 */
public class Polygon extends AbstractGeoJsonItem implements GeometryWithCoordinateSupport<List<Position>> {
    private LineString value;

    public Coordinates<List<Position>> getCoordinates() {
        return value.getCoordinates();
    }
}

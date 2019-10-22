package org.openstreetmap.atlas.geography.geojson.concrete.geometry;

import org.openstreetmap.atlas.geography.geojson.concrete.AbstractGeoJsonItem;

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

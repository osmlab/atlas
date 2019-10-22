package org.openstreetmap.atlas.geography.geojson.concrete.geometry;

import org.openstreetmap.atlas.geography.geojson.concrete.AbstractGeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.concrete.type.GeometryType;

import java.util.List;

/**
 * @author Yazad Khambata
 */
public class MultiPoint extends AbstractGeometry implements GeometryWithCoordinateSupport<List<Position>> {
    private Coordinates<List<Position>> coordinates; //one or more.

    @Override
    public Coordinates<List<Position>> getCoordinates() {
        return coordinates;
    }
}
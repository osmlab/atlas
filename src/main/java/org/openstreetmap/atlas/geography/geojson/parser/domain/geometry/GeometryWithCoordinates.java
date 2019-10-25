package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;

/**
 * @author Yazad Khambata
 */
public interface GeometryWithCoordinates<C> extends Geometry {
    Coordinates<C> getCoordinates();
}

package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

/**
 * @author Yazad Khambata
 */
public interface GeometryWithCoordinateSupport<C> extends Geometry {
    Coordinates<C> getCoordinates();
}

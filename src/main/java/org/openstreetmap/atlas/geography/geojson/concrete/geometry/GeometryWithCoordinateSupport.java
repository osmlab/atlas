package org.openstreetmap.atlas.geography.geojson.concrete.geometry;

/**
 * @author Yazad Khambata
 */
public interface GeometryWithCoordinateSupport<C> extends Geometry {
    Coordinates<C> getCoordinates();
}

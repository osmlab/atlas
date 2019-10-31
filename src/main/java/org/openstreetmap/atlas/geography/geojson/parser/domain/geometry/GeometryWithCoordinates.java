package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;

/**
 * An abstraction of a geometry with coordinates.
 *
 * @param <C>
 *            - value of the coordinates.
 * @author Yazad Khambata
 */
public interface GeometryWithCoordinates<C> extends Geometry
{
    Coordinates<C> getCoordinates();
}

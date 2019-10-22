package org.openstreetmap.atlas.geography.geojson.concrete.geometry;

import java.util.List;

/**
 * {@link GeometryCollection} nesting inside other {@link GeometryCollection}(s) is NOT allowed.
 *
 * @author Yazad Khambata
 */
public class GeometryCollection extends AbstractGeometry {
    private List<GeometryWithCoordinateSupport> geometries;
}

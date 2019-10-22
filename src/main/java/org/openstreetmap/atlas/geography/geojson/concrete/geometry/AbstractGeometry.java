package org.openstreetmap.atlas.geography.geojson.concrete.geometry;

import org.openstreetmap.atlas.geography.geojson.concrete.AbstractGeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.concrete.type.GeometryType;

/**
 * @author Yazad Khambata
 */
public class AbstractGeometry extends AbstractGeoJsonItem implements Geometry {
    AbstractGeometry() {
        super(GeometryType.class);
    }
}

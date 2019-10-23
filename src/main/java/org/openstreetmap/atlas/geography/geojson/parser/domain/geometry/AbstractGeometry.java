package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.AbstractGeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.GeometryType;

/**
 * @author Yazad Khambata
 */
public class AbstractGeometry extends AbstractGeoJsonItem implements Geometry {
    AbstractGeometry() {
        super(GeometryType.class);
    }
}

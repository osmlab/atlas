package org.openstreetmap.atlas.geography.geojson.parser.domain.feature;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.AbstractGeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.type.FeatureType;

/**
 * @author Yazad Khambata
 */
public class AbstractFeature extends AbstractGeoJsonItem {
    AbstractFeature() {
        super(FeatureType.class);
    }
}

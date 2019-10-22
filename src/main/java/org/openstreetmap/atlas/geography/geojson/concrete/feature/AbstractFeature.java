package org.openstreetmap.atlas.geography.geojson.concrete.feature;

import org.openstreetmap.atlas.geography.geojson.concrete.AbstractGeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.concrete.type.FeatureType;

/**
 * @author Yazad Khambata
 */
public class AbstractFeature extends AbstractGeoJsonItem {
    AbstractFeature() {
        super(FeatureType.class);
    }
}

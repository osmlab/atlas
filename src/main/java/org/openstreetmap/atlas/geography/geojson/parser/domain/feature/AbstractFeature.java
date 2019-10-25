package org.openstreetmap.atlas.geography.geojson.parser.domain.feature;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.AbstractGeoJsonItem;

import java.util.Map;

/**
 * @author Yazad Khambata
 */
public abstract class AbstractFeature extends AbstractGeoJsonItem {
    AbstractFeature(final Map<String, Object> map) {
        super(map);
    }
}

package org.openstreetmap.atlas.geography.geojson.parser;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;

import java.util.Map;

/**
 * @author Yazad Khambata
 */
public interface GoeJsonParser {
    GeoJsonItem deserialize(final String geoJson);

    GeoJsonItem deserialize(final Map<String, Object> map);
}

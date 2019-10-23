package org.openstreetmap.atlas.geography.geojson.parser;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;

/**
 * @author Yazad Khambata
 */
public interface GoeJsonParser {
    GeoJsonItem deserialize(final String geoJson);
}

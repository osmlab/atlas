package org.openstreetmap.atlas.geography.geojson.parser;

import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;

/**
 * @author Yazad Khambata
 */
public interface GoeJsonParser
{
    GeoJsonItem deserialize(String geoJson);

    GeoJsonItem deserialize(Map<String, Object> map);
}

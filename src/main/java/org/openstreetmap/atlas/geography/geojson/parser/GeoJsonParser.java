package org.openstreetmap.atlas.geography.geojson.parser;

import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;

/**
 * @author Yazad Khambata
 */
public interface GeoJsonParser
{
    GeoJsonItem deserialize(String geoJson);

    GeoJsonItem deserialize(Map<String, Object> map);
    
    <T> T deserializeExtension(final String json, Class<T> targetClass);

    <T> T deserializeExtension(Map<String, Object> map, Class<T> targetClass);
}

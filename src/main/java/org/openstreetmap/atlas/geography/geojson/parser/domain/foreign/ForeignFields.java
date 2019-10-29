package org.openstreetmap.atlas.geography.geojson.parser.domain.foreign;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Yazad Khambata
 */
public interface ForeignFields extends Serializable
{
    Map<String, Object> asMap();

    <T> T get(String key, Class<T> valueClass);

    Object get(String key);
}

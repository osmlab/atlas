package org.openstreetmap.atlas.geography.geojson.parser.domain.foreign;

import java.io.Serializable;

/**
 * @author Yazad Khambata
 */
public interface ForeignFields extends Serializable {
    Object get(String key);

    <T> T get(String key, Class<T> valueClass);

    void put(String key, String value);
}

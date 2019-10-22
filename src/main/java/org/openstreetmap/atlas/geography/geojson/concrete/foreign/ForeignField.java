package org.openstreetmap.atlas.geography.geojson.concrete.foreign;

import java.io.Serializable;

/**
 * @author Yazad Khambata
 */
public interface ForeignField extends Serializable {
    Object get(String key);

    <T> T get(String key, Class<T> valueClass);

    void put(String key, String value);
}

package org.openstreetmap.atlas.geography.geojson.concrete.properties;

import org.openstreetmap.atlas.geography.geojson.concrete.foreign.ForeignField;

/**
 * @author Yazad Khambata
 */
public class Properties implements ForeignField {
    private ForeignField values;

    @Override
    public Object get(final String key) {
        return values.get(key);
    }

    @Override
    public <T> T get(final String key, final Class<T> valueClass) {
        return values.get(key, valueClass);
    }

    @Override
    public void put(final String key, final String value) {
        values.put(key, value);
    }
}

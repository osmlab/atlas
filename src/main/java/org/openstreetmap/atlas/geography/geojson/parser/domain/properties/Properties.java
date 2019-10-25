package org.openstreetmap.atlas.geography.geojson.parser.domain.properties;

import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;

/**
 * @author Yazad Khambata
 */
public class Properties implements ForeignFields {
    private ForeignFields values;

    @Override
    public Object get(final String key) {
        return values.get(key);
    }

    @Override
    public <T> T get(final String key, final Class<T> valueClass) {
        return values.get(key, valueClass);
    }

    @Override
    public void put(final String key, final Object value) {
        values.put(key, value);
    }
}

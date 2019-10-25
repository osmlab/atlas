package org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext;

import org.openstreetmap.atlas.geography.geojson.parser.domain.annotation.Foreign;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;

/**
 * @author Yazad Khambata
 */
@Foreign
public class Metadata implements ForeignFields
{
    private ForeignFields values;

    @Override
    public Object get(final String key)
    {
        return this.values.get(key);
    }

    @Override
    public <T> T get(final String key, final Class<T> valueClass)
    {
        return this.values.get(key, valueClass);
    }

    @Override
    public void put(final String key, final Object value)
    {
        this.values.put(key, value);
    }
}

package org.openstreetmap.atlas.geography.geojson.parser.domain.foreign;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Yazad Khambata
 */
public class DefaultForeignFieldsImpl implements ForeignFields
{
    private Map<String, Object> valuesAsMap;

    @Override
    public Object get(final String key)
    {
        return this.valuesAsMap.get(key);
    }

    @Override
    public <T> T get(final String key, final Class<T> valueClass)
    {
        return (T) this.valuesAsMap.get(key);
    }

    @Override
    public void put(final String key, final Object value)
    {
        this.valuesAsMap.put(key, value);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}

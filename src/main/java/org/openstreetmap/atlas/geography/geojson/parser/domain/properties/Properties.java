package org.openstreetmap.atlas.geography.geojson.parser.domain.properties;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.DefaultForeignFieldsImpl;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;
import org.openstreetmap.atlas.geography.geojson.parser.mapper.Mapper;
import org.openstreetmap.atlas.geography.geojson.parser.mapper.impl.DefaultBeanUtilsBasedMapperImpl;

/**
 * @author Yazad Khambata
 */
public class Properties implements ForeignFields
{
    private ForeignFields values;

    public Properties(final Map<String, Object> valuesAsMap)
    {
        this.values = new DefaultForeignFieldsImpl(valuesAsMap);
    }

    @Override
    public Map<String, Object> asMap()
    {
        final Map<String, Object> foreignMap = this.values.asMap();
        if (foreignMap == null)
        {
            return Collections.EMPTY_MAP;
        }

        return Collections.unmodifiableMap(foreignMap);
    }

    public <T> T asType(final Class<T> type, final Mapper mapper)
    {
        return mapper.map(this.asMap(), type);
    }

    public <T> T asType(final Class<T> type)
    {
        return asType(type, DefaultBeanUtilsBasedMapperImpl.instance);
    }

    @Override
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }

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
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}

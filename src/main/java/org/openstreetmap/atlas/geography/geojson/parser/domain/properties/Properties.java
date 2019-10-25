package org.openstreetmap.atlas.geography.geojson.parser.domain.properties;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.DefaultForeignFieldsImpl;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Yazad Khambata
 */
public class Properties implements ForeignFields {
    private ForeignFields values;

    public Properties(final Map<String, Object> valuesAsMap) {
        this(valuesAsMap, new HashSet<>());
    }

    public Properties(final Map<String, Object> valuesAsMap, final Set<String> excludeFields) {
        //TODO: exclude logic.

        this.values = new DefaultForeignFieldsImpl(valuesAsMap);
    }

    @Override
    public Object get(final String key) {
        return this.values.get(key);
    }

    @Override
    public <T> T get(final String key, final Class<T> valueClass) {
        return this.values.get(key, valueClass);
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object that) {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}

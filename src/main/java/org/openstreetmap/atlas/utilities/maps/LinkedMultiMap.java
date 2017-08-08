package org.openstreetmap.atlas.utilities.maps;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

/**
 * A multimap using LinkedHashMap and LinkedHashSet
 *
 * @author cuthbertm
 * @param <K>
 *            The key type for the map
 * @param <V>
 *            The value type for the map
 */
public class LinkedMultiMap<K, V> implements Map<K, Set<V>>, Serializable
{
    private static final long serialVersionUID = 5668281262269816871L;

    private final Map<K, Set<V>> linkedMap = new LinkedHashMap<>();

    public void add(final K key, final V value)
    {
        Set<V> values = this.linkedMap.get(key);
        if (values == null)
        {
            values = new LinkedHashSet<>();
            this.linkedMap.put(key, values);
        }
        if (value != null)
        {
            values.add(value);
        }
    }

    @Override
    public void clear()
    {
        this.linkedMap.clear();
    }

    @Override
    public boolean containsKey(final Object key)
    {
        return this.linkedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value)
    {
        return this.linkedMap.entrySet().stream().filter(entry -> entry.getValue().contains(value))
                .collect(Collectors.counting()) > 0;
    }

    @Override
    public Set<Entry<K, Set<V>>> entrySet()
    {
        return this.linkedMap.entrySet();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<V> get(final Object key)
    {
        final Set<V> values = this.linkedMap.get(key);
        return values == null ? Collections.EMPTY_SET : values;
    }

    @Override
    public boolean isEmpty()
    {
        return this.linkedMap.isEmpty();
    }

    @Override
    public Set<K> keySet()
    {
        return this.linkedMap.keySet();
    }

    @Override
    public Set<V> put(final K key, final Set<V> value)
    {
        return this.linkedMap.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends Set<V>> map)
    {
        this.linkedMap.putAll(map);
    }

    @Override
    public Set<V> remove(final Object key)
    {
        return this.linkedMap.remove(key);
    }

    @Override
    public boolean remove(final Object key, final Object value)
    {
        final Set<V> values = this.linkedMap.get(key);
        if (values != null && values.contains(value))
        {
            values.remove(value);
            return true;
        }
        return false;
    }

    @Override
    public int size()
    {
        return this.linkedMap.size();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        String entryDelimiter = "";
        builder.append("[");
        for (final Entry<K, Set<V>> entry : this.linkedMap.entrySet())
        {
            final K key = entry.getKey();
            final Set<V> values = entry.getValue();

            builder.append(entryDelimiter);
            builder.append(key.toString());
            builder.append(" -> ");

            if (values != null && !values.isEmpty())
            {
                Joiner.on(", ").skipNulls().appendTo(builder, values);
            }
            else
            {
                builder.append("Empty values");
            }
            entryDelimiter = ", ";
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Collection<Set<V>> values()
    {
        return this.linkedMap.values();
    }
}

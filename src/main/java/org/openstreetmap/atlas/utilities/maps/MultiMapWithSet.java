package org.openstreetmap.atlas.utilities.maps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple SetMultiMap backed by a {@link HashMap} and {@link HashSet}s
 *
 * @author matthieun
 * @param <K>
 *            The Key type
 * @param <V>
 *            The Value type
 */
public class MultiMapWithSet<K, V> implements Map<K, Set<V>>, Serializable
{
    private static final long serialVersionUID = -8408806495092086637L;

    private final Map<K, Set<V>> map;

    public MultiMapWithSet()
    {
        this.map = new HashMap<>();
    }

    /**
     * If it does not already contain this key, add the key. Then add the value to the key.
     *
     * @param key
     *            The key to add
     * @param value
     *            The value to add to the key
     */
    public void add(final K key, final V value)
    {
        if (!this.map.containsKey(key))
        {
            this.map.put(key, new HashSet<V>());
        }
        this.map.get(key).add(value);
    }

    public List<V> allValues()
    {
        final List<V> result = new ArrayList<>();
        for (final Set<V> valueList : values())
        {
            result.addAll(valueList);
        }
        return result;
    }

    @Override
    public void clear()
    {
        this.map.clear();
    }

    @Override
    public boolean containsKey(final Object key)
    {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<java.util.Map.Entry<K, Set<V>>> entrySet()
    {
        return this.map.entrySet();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof MultiMapWithSet)
        {
            return this.map.equals(((MultiMapWithSet<?, ?>) other).map);
        }
        return false;
    }

    @Override
    public Set<V> get(final Object key)
    {
        return this.map.get(key);
    }

    @Override
    public int hashCode()
    {
        return this.map.hashCode();
    }

    @Override
    public boolean isEmpty()
    {
        return this.map.isEmpty();
    }

    @Override
    public Set<K> keySet()
    {
        return this.map.keySet();
    }

    @Override
    public Set<V> put(final K key, final Set<V> value)
    {
        return this.map.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends Set<V>> map)
    {
        this.map.putAll(map);
    }

    public void putAll(final MultiMapWithSet<K, V> map)
    {
        putAll(map.getMap());
    }

    @Override
    public Set<V> remove(final Object key)
    {
        return this.map.remove(key);
    }

    @Override
    public int size()
    {
        return this.map.size();
    }

    @Override
    public String toString()
    {
        return this.map.toString();
    }

    @Override
    public Collection<Set<V>> values()
    {
        return this.map.values();
    }

    private Map<K, Set<V>> getMap()
    {
        return this.map;
    }
}

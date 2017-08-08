package org.openstreetmap.atlas.utilities.maps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Simple MultiMap backed by a {@link HashMap} and {@link ArrayList}s
 *
 * @author matthieun
 * @param <K>
 *            The Key type
 * @param <V>
 *            The Value type
 */
public class MultiMap<K, V> implements Map<K, List<V>>, Serializable
{
    private static final long serialVersionUID = -8408806495092086637L;

    private final Map<K, List<V>> map;

    public MultiMap()
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
            this.map.put(key, new ArrayList<V>());
        }
        this.map.get(key).add(value);
    }

    /**
     * Merge all the values of another {@link MultiMap}
     *
     * @param other
     *            The other {@link MultiMap} to merge
     */
    public void addAll(final MultiMap<K, V> other)
    {
        for (final K key : other.keySet())
        {
            this.put(key, new ArrayList<>());
            for (final V value : other.get(key))
            {
                this.add(key, value);
            }
        }
    }

    public List<V> allValues()
    {
        final List<V> result = new ArrayList<>();
        for (final List<V> valueList : values())
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
    public Set<java.util.Map.Entry<K, List<V>>> entrySet()
    {
        return this.map.entrySet();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof MultiMap)
        {
            return this.map.equals(((MultiMap<?, ?>) other).map);
        }
        return false;
    }

    @Override
    public List<V> get(final Object key)
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
    public List<V> put(final K key, final List<V> value)
    {
        return this.map.put(key, value);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends List<V>> map)
    {
        this.map.putAll(map);
    }

    public void putAll(final MultiMap<K, V> map)
    {
        putAll(map.getMap());
    }

    public Map<K, V> reduceByKey(final BiFunction<V, V, V> reducer)
    {
        final Map<K, V> result = new HashMap<>();
        for (final K key : keySet())
        {
            final List<V> values = get(key);
            if (values.isEmpty())
            {
                continue;
            }
            V token = null;
            for (final V value : values)
            {
                if (token == null)
                {
                    token = value;
                }
                else
                {
                    token = reducer.apply(token, value);
                }
            }
            result.put(key, token);
        }
        return result;
    }

    @Override
    public List<V> remove(final Object key)
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
    public Collection<List<V>> values()
    {
        return this.map.values();
    }

    private Map<K, List<V>> getMap()
    {
        return this.map;
    }
}

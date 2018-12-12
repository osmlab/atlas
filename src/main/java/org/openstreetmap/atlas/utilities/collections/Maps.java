package org.openstreetmap.atlas.utilities.collections;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public final class Maps
{
    /**
     * Return a {@link HashMap} from a even number of keys and values of the same type
     *
     * @param items
     *            a even number of keys and values of the same type
     * @param <T>
     *            The type for the map
     * @return A {@link HashMap} translated from the keys and values in items
     */
    @SafeVarargs
    public static <T> Map<T, T> hashMap(final T... items)
    {
        if (items.length % 2 != 0)
        {
            throw new CoreException("Needs to have an even number of arguments");
        }
        final Map<T, T> result = new HashMap<>();
        for (int i = 0; i < items.length; i += 2)
        {
            result.put(items[i], items[i + 1]);
        }
        return result;
    }

    public static Map<String, String> stringMap(final String... items)
    {
        return hashMap(items);
    }

    @SafeVarargs
    public static <K, V> Map<K, V> withMaps(final boolean rejectCollisions,
            final Map<K, V>... items)
    {
        if (items.length == 0)
        {
            return new HashMap<>();
        }
        if (items.length == 1)
        {
            return items[0];
        }
        final Map<K, V> result = new HashMap<>();
        for (final Map<K, V> item : items)
        {
            for (final Map.Entry<K, V> entry : item.entrySet())
            {
                if (rejectCollisions && result.containsKey(entry.getKey()))
                {
                    throw new CoreException("Cannot merge maps! Collision on key.");
                }
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> withMaps(final Map<K, V>... items)
    {
        return withMaps(true, items);
    }

    private Maps()
    {
    }
}

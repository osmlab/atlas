package org.openstreetmap.atlas.utilities.collections;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public final class Sets
{
    @SafeVarargs
    public static <T> Set<T> hashSet(final T... elements)
    {
        final Set<T> result = new HashSet<>();
        for (final T element : elements)
        {
            result.add(element);
        }
        return result;
    }

    @SafeVarargs
    public static <T extends Comparable<T>> SortedSet<T> treeSet(final T... elements)
    {
        final SortedSet<T> result = new TreeSet<>();
        for (final T element : elements)
        {
            result.add(element);
        }
        return result;
    }

    @SafeVarargs
    public static <V> Set<V> withSets(final boolean rejectCollisions, final Set<V>... items)
    {
        if (items.length == 0)
        {
            return new HashSet<>();
        }
        if (items.length == 1)
        {
            return items[0];
        }
        final Set<V> result = new HashSet<>();
        for (final Set<V> item : items)
        {
            for (final V entry : item)
            {
                if (rejectCollisions && result.contains(entry))
                {
                    throw new CoreException("Cannot merge sets! Collision on element.");
                }
                result.add(entry);
            }
        }
        return result;
    }

    @SafeVarargs
    public static <V> Set<V> withSets(final Set<V>... items)
    {
        return withSets(true, items);
    }

    @SafeVarargs
    public static <V> SortedSet<V> withSortedSets(final boolean rejectCollisions,
            final SortedSet<V>... items)
    {
        if (items.length == 0)
        {
            return new TreeSet<>();
        }
        if (items.length == 1)
        {
            return items[0];
        }
        final SortedSet<V> result = new TreeSet<>();
        for (final SortedSet<V> item : items)
        {
            for (final V entry : item)
            {
                if (rejectCollisions && result.contains(entry))
                {
                    throw new CoreException("Cannot merge sets! Collision on element.");
                }
                result.add(entry);
            }
        }
        return result;
    }

    @SafeVarargs
    public static <V> SortedSet<V> withSortedSets(final SortedSet<V>... items)
    {
        return withSortedSets(true, items);
    }

    private Sets()
    {
    }
}

package org.openstreetmap.atlas.utilities.collections;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

    private Sets()
    {
    }
}

package org.openstreetmap.atlas.utilities.collections;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

/**
 * Shamelessly stolen from:
 * http://stackoverflow.com/questions/29090277/java-8-streams-collections-tomap-from-list-how-to-
 * keep-the-order
 *
 * @author cstaylor
 */
public final class EnhancedCollectors
{
    public static <T extends Comparable<T>> Collector<T, ?, ImmutableList<T>> toImmutableList()
    {
        return new ImmutableListCollector<>();
    }

    /**
     * I wanted a way of quickly converting lists to linked hashmaps, so I found this little block
     * of code in Stack Overflow
     *
     * @param <T>
     *            the type in the incoming list
     * @param <K>
     *            the key type of the outgoing map
     * @param <U>
     *            the value type of the outgoing map
     * @param keyMapper
     *            how we get the key into the map
     * @param valueMapper
     *            how we get the value into the map
     * @return the linked hashmap
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper, valueMapper, (key, value) ->
        {
            throw new IllegalStateException(String.format("Duplicate key %s", key));
        }, LinkedHashMap::new);
    }

    public static <T, K extends Comparable<K>, U> Collector<T, ?, SortedMap<K, U>> toUnmodifiableSortedMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper)
    {
        return new UnmodifiableSortedMapCollector<>(keyMapper, valueMapper);
    }

    public static <T extends Comparable<T>> Collector<T, ?, SortedSet<T>> toUnmodifiableSortedSet()
    {
        return new UnmodifiableSortedSetCollector<>();
    }

    private EnhancedCollectors()
    {

    }
}

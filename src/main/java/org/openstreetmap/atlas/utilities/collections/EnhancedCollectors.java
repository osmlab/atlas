package org.openstreetmap.atlas.utilities.collections;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

/**
 * Shamelessly stolen from:
 * http://stackoverflow.com/questions/29090277/java-8-streams-collections-tomap-from-list-how-to-
 * keep-the-order and from:
 * https://stackoverflow.com/questions/39130122/java-8-nested-multi-level-group-by/39131049#39131049
 *
 * @author cstaylor
 * @author mgostintsev
 */
public final class EnhancedCollectors
{
    public static <T, K, A, R> Collector<T, ?, R> flatMapping(
            final Function<? super T, ? extends Stream<? extends K>> mapper,
            final Collector<? super K, A, R> downstream)
    {
        final BiConsumer<A, ? super K> accumulator = downstream.accumulator();
        return Collector.of(downstream.supplier(), (itemA, itemT) ->
        {
            try (Stream<? extends K> s = mapper.apply(itemT))
            {
                if (s != null)
                {
                    s.forEachOrdered(u -> accumulator.accept(itemA, u));
                }
            }
        }, downstream.combiner(), downstream.finisher(),
                downstream.characteristics().stream().toArray(Collector.Characteristics[]::new));
    }

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

package org.openstreetmap.atlas.utilities.collections;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.Ordering;

/**
 * Converts a stream of objects into an immutable map
 *
 * @author cstaylor
 * @param <T>
 *            the type of incoming objects we want to map
 * @param <K>
 *            the type of keys stored in the map
 * @param <U>
 *            the values stored in the map
 */
public class UnmodifiableSortedMapCollector<T, K extends Comparable<K>, U>
        implements Collector<T, SortedMap<K, U>, SortedMap<K, U>>
{
    private final Function<? super T, ? extends K> keyMapper;
    private final Function<? super T, ? extends U> valueMapper;

    public UnmodifiableSortedMapCollector(final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper)
    {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    @Override
    public BiConsumer<SortedMap<K, U>, T> accumulator()
    {
        return (builder, item) -> builder.put(this.keyMapper.apply(item),
                this.valueMapper.apply(item));
    }

    @Override
    public Set<Characteristics> characteristics()
    {
        return EnumSet.of(Characteristics.UNORDERED);
    }

    @Override
    public BinaryOperator<SortedMap<K, U>> combiner()
    {
        return (builder1, builder2) ->
        {
            builder1.putAll(builder2);
            return builder1;
        };
    }

    @Override
    public Function<SortedMap<K, U>, SortedMap<K, U>> finisher()
    {
        return original -> Collections.unmodifiableSortedMap(original);
    }

    @Override
    public Supplier<SortedMap<K, U>> supplier()
    {
        return () -> new TreeMap<>(Ordering.natural());
    }
}

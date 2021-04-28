package org.openstreetmap.atlas.utilities.collections;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.Ordering;

/**
 * Converts a stream of objects into an immutable set
 *
 * @author cstaylor
 * @param <U>
 *            the values stored in the map
 */
public class UnmodifiableSortedSetCollector<U extends Comparable<U>>
        implements Collector<U, SortedSet<U>, SortedSet<U>>
{
    @Override
    public BiConsumer<SortedSet<U>, U> accumulator()
    {
        return (builder, item) -> builder.add(item);
    }

    @Override
    public Set<Characteristics> characteristics()
    {
        return EnumSet.of(Characteristics.UNORDERED);
    }

    @Override
    public BinaryOperator<SortedSet<U>> combiner()
    {
        return (builder1, builder2) ->
        {
            builder1.addAll(builder2);
            return builder1;
        };
    }

    @Override
    public Function<SortedSet<U>, SortedSet<U>> finisher()
    {
        return set -> Collections.unmodifiableSortedSet(set);
    }

    @Override
    public Supplier<SortedSet<U>> supplier()
    {
        return () -> new TreeSet<>(Ordering.natural());
    }
}

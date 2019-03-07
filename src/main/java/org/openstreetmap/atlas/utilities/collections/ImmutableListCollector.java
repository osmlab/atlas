package org.openstreetmap.atlas.utilities.collections;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

/**
 * Converts a stream of objects into an {@link ImmutableList}
 *
 * @author mgostintsev
 * @param <T>
 *            the type of incoming objects we want in the {@link ImmutableList}
 */
public class ImmutableListCollector<T extends Comparable<T>>
        implements Collector<T, ImmutableList.Builder<T>, ImmutableList<T>>
{

    @Override
    public BiConsumer<Builder<T>, T> accumulator()
    {
        return (builder, item) -> builder.add(item);
    }

    @Override
    public Set<java.util.stream.Collector.Characteristics> characteristics()
    {
        return ImmutableSet.of(Characteristics.UNORDERED);
    }

    @Override
    public BinaryOperator<Builder<T>> combiner()
    {
        return (builder1, builder2) ->
        {
            builder1.addAll(builder2.build());
            return builder1;
        };
    }

    @Override
    public Function<Builder<T>, ImmutableList<T>> finisher()
    {
        return builder -> builder.build();
    }

    @Override
    public Supplier<Builder<T>> supplier()
    {
        return ImmutableList::builder;
    }

}

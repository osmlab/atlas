package org.openstreetmap.atlas.utilities.collections;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An {@link Iterable} that offers similar methods as the {@link Stream} API. To construct one, use
 * {@link Iterables}:
 * <p>
 * <code>
 * Iterables.stream(someIterable).map(...).filter(...).collect();
 * </code> Note: StreamIterable is not thread safe with parallelization usage.
 *
 * @author matthieun
 * @param <T>
 *            The type of the {@link Iterable}
 */
public class StreamIterable<T> implements Iterable<T>
{
    private final Iterable<T> source;
    private boolean parallel = false;

    protected StreamIterable(final Iterable<T> source)
    {
        this.source = source;
    }

    /**
     * Construct a new StreamIterable.
     *
     * @param source
     *            The source iterable to construct the StreamIterable from
     * @param parallel
     *            Controls whether to use parallelization or not when streaming
     */
    protected StreamIterable(final Iterable<T> source, final boolean parallel)
    {
        this.source = source;
        this.parallel = parallel;
    }

    /**
     * Test whether all elements from iterable match the given predicate.
     *
     * @param predicate
     *            Predicate to test
     * @return {@code true} when given predicate is true for all entities in iterable, else false
     */
    public boolean allMatch(final Predicate<T> predicate)
    {
        return StreamSupport.stream(this.source.spliterator(), this.parallel).allMatch(predicate);
    }

    /**
     * Test whether any of the elements from iterable matches the given predicate
     *
     * @param predicate
     *            Predicate to test
     * @return {@code true} when given predicate is true for any one entity in iterable, else false
     */
    public boolean anyMatch(final Predicate<T> predicate)
    {
        return StreamSupport.stream(this.source.spliterator(), this.parallel).anyMatch(predicate);
    }

    /**
     * @return The original {@link Iterable} from this {@link StreamIterable}
     */
    public Iterable<T> collect()
    {
        return this.source;
    }

    /**
     * @return The original {@link Iterable} from this {@link StreamIterable}, collected into a
     *         {@link List}
     */
    public List<T> collectToList()
    {
        return Iterables.asList(this.source);
    }

    /**
     * @return The original {@link Iterable} from this {@link StreamIterable}, collected into a
     *         {@link Set}
     */
    public Set<T> collectToSet()
    {
        return Iterables.asSet(this.source);
    }

    /**
     * @return The original {@link Iterable} from this {@link StreamIterable}, collected into a
     *         {@link SortedSet}
     */
    public SortedSet<T> collectToSortedSet()
    {
        return Iterables.asSortedSet(this.source);
    }

    /**
     * Disable parallelization in streams from this StreamIterator
     *
     * @return The StreamIterator with parallelization disabled
     */
    public StreamIterable<T> disableParallelization()
    {
        this.parallel = false;
        return this;
    }

    /**
     * Enable parallelization in streams from this StreamIterator
     *
     * @return The StreamIterator with parallelization enabled
     */
    public StreamIterable<T> enableParallelization()
    {
        this.parallel = true;
        return this;
    }

    /**
     * Filter an {@link Iterable}
     *
     * @param filter
     *            The filter function
     * @return The filtered {@link Iterable} as a {@link StreamIterable}
     */
    public StreamIterable<T> filter(final Predicate<T> filter)
    {
        return new StreamIterable<>(Iterables.filter(this.source, filter), this.parallel);
    }

    /**
     * Filter an {@link Iterable} using a set of known elements to filter and an idenfitier function
     *
     * @param filterSet
     *            The set of IdentifierTypes to filter
     * @param identifier
     *            The function mapping an element of type T to its identifier of type IdentifierType
     * @param <IdentifierType>
     *            The type for the object identifier for elements of the {@link Iterable}
     * @return The filtered {@link Iterable} as a {@link StreamIterable}
     */
    public <IdentifierType> StreamIterable<T> filter(final Set<IdentifierType> filterSet,
            final Function<T, IdentifierType> identifier)
    {
        return new StreamIterable<>(
                new FilteredIterable<T, IdentifierType>(this.source, filterSet, identifier),
                this.parallel);
    }

    /**
     * Flat Map an {@link Iterable}. This means each input item can return one to many results.
     *
     * @param <V>
     *            The type of the output {@link Iterable}
     * @param flatMap
     *            The function to flat map
     * @return The flat mapped {@link Iterable}
     */
    public <V> StreamIterable<V> flatMap(final Function<T, Iterable<? extends V>> flatMap)
    {
        return new StreamIterable<>(Iterables.translateMulti(this.source, flatMap), this.parallel);
    }

    @Override
    public Iterator<T> iterator()
    {
        return this.source.iterator();
    }

    /**
     * Map an {@link Iterable} to a value
     *
     * @param <V>
     *            The new type of the output {@link Iterable}
     * @param map
     *            The map function
     * @return The mapped {@link Iterable}
     */
    public <V> StreamIterable<V> map(final Function<T, V> map)
    {
        return new StreamIterable<>(Iterables.translate(this.source, map), this.parallel);
    }

    /**
     * Truncate an {@link Iterable} from start and end
     *
     * @param startIndex
     *            The index before which to truncate from the start
     * @param indexFromEnd
     *            The index after which to truncate from the end
     * @return The truncated {@link Iterable}
     */
    public StreamIterable<T> truncate(final int startIndex, final int indexFromEnd)
    {
        return new StreamIterable<>(Iterables.truncate(this.source, startIndex, indexFromEnd),
                this.parallel);
    }
}

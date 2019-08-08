package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.StreamSupport;

/**
 * Iterable utility methods
 *
 * @author matthieun
 */
public final class Iterables
{
    /**
     * Adds the contents of the from Iterable to the addHere collection, and returns true if items
     * were added. It's possible that nothing has changed if addHere is a set
     *
     * @param addHere
     *            where to add the items
     * @param from
     *            where to get the items
     * @param <T>
     *            what kind of objects we're copying
     * @return true if addHere has changed, false otherwise
     */
    public static <T> boolean addAll(final Collection<T> addHere, final Iterable<T> from)
    {
        final int oldSize = addHere.size();
        StreamSupport.stream(from.spliterator(), false).forEach(addHere::add);
        return oldSize < addHere.size();
    }

    /**
     * Strip down any {@link Iterable} into .. just an {@link Iterable}.
     *
     * @param types
     *            The {@link Iterable} to strip down
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T> Iterable<T> asIterable(final Iterable<T> types)
    {
        return types::iterator;
    }

    /**
     * Translate an {@link Iterable} into a {@link List}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T> List<T> asList(final Iterable<T> types)
    {
        if (types instanceof List)
        {
            return (List<T>) types;
        }
        final List<T> result = new ArrayList<>();
        types.forEach(result::add);
        return result;
    }

    /**
     * Translate an array to an {@link List}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T> List<T> asList(final T[] types)
    {
        final List<T> result = new ArrayList<>();
        for (final T type : types)
        {
            result.add(type);
        }
        return result;
    }

    /**
     * Translate an iterable list of Map entries to a map
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <K>
     *            The type of key of the entry
     * @param <V>
     *            The type of value of the entry
     * @return The translated {@link Iterable}
     */
    public static <K, V> Map<K, V> asMap(final Iterable<Map.Entry<K, V>> types)
    {
        final Map<K, V> result = new HashMap<>();
        types.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    /**
     * Translate an {@link Iterable} into a {@link Queue}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T> Queue<T> asQueue(final Iterable<T> types)
    {
        final Queue<T> result = new LinkedList<>();
        types.forEach(result::add);
        return result;
    }

    /**
     * Translate an {@link Iterable} into a {@link Set}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T> Set<T> asSet(final Iterable<T> types)
    {
        final Set<T> result = new HashSet<>();
        types.forEach(result::add);
        return result;
    }

    /**
     * Translate an array to an {@link Set}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T> Set<T> asSet(final T[] types)
    {
        final Set<T> result = new HashSet<>();
        for (final T type : types)
        {
            result.add(type);
        }
        return result;
    }

    /**
     * Translate an {@link Iterable} of items into a {@link SortedSet}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T> SortedSet<T> asSortedSet(final Iterable<T> types)
    {
        final SortedSet<T> result = new TreeSet<>();
        types.forEach(result::add);
        return result;
    }

    /**
     * Test if an {@link Iterable} iterates at some point on an item.
     *
     * @param types
     *            The {@link Iterable} to test
     * @param type
     *            The item to test
     * @param <T>
     *            The type of the {@link Iterable}
     * @return True if the {@link Iterable} iterates at some point on the item.
     */
    public static <T> boolean contains(final Iterable<T> types, final T type)
    {
        if (types instanceof Collection)
        {
            return ((Collection<T>) types).contains(type);
        }
        for (final T candidate : types)
        {
            if (candidate.equals(type))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Count a set of values
     *
     * @param types
     *            The {@link Iterable} of input type
     * @param typeCounter
     *            The function from type to count
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The total count
     */
    public static <T> long count(final Iterable<T> types, final ToLongFunction<T> typeCounter)
    {
        long result = 0;
        for (final T type : types)
        {
            result += typeCounter.applyAsLong(type);
        }
        return result;
    }

    /**
     * @param example
     *            A random object to specify the type
     * @param <T>
     *            The type of the {@link Iterable}
     * @return An empty {@link Iterable} of the right type
     */
    public static <T> Iterable<T> emptyIterable(final T example) // NOSONAR
    {
        return () -> new Iterator<T>()
        {
            @Override
            public boolean hasNext()
            {
                return false;
            }

            @Override
            public T next()
            {
                throw new NoSuchElementException();
            }
        };
    }

    /**
     * Test if two {@link Iterable}s iterate on the same items.
     *
     * @param that
     *            The first {@link Iterable}
     * @param other
     *            The second iterable
     * @param <T>
     *            The type of the {@link Iterable}
     * @return True if the two {@link Iterable}s iterate on the same items.
     */
    public static <T> boolean equals(final Iterable<T> that, final Iterable<T> other)
    {
        // Handle null iterables
        // If they are both null, then equal
        // If only one of them is null, then NOT equal
        final boolean thatIsNull = that == null;
        final boolean otherIsNull = other == null;
        if (thatIsNull || otherIsNull)
        {
            return thatIsNull && otherIsNull;
        }

        // Iterables are not null, let's check for size first
        // Then the values
        final long thatSize = Iterables.size(that);
        if (thatSize != Iterables.size(other))
        {
            return false;
        }
        final Iterator<T> thatIterator = that.iterator();
        final Iterator<T> otherIterator = other.iterator();
        while (thatIterator.hasNext())
        {
            if (!thatIterator.next().equals(otherIterator.next()))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Filter an {@link Iterable}
     *
     * @param input
     *            The {@link Iterable} to filter
     * @param matcher
     *            The {@link Predicate} used to filter
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The filtered {@link Iterable}
     */
    public static <T> Iterable<T> filter(final Iterable<T> input, final Predicate<T> matcher)
    {
        return filterTranslate(input, item -> item, matcher);
    }

    /**
     * Translate an {@link Iterable} of items into a {@link FilteredIterable}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param filterSet
     *            A set of identifiers for elements to skip (can be empty or have members)
     * @param identifier
     *            A function that takes an element of T for the {@link Iterable} and returns the
     *            identifier for that element
     * @param <T>
     *            The type of the {@link Iterable}
     * @param <I>
     *            The type of the Identifier object for the elements in the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T, I> FilteredIterable<T, I> filter(final Iterable<T> types,
            final Set<I> filterSet, final Function<T, I> identifier)
    {
        return new FilteredIterable<>(types, filterSet, identifier);
    }

    /**
     * Translate an {@link Iterable} of type I to an {@link Iterable} of type O.
     *
     * @param input
     *            The input {@link Iterable}
     * @param converter
     *            The converter from I to O
     * @param matcher
     *            A {@link Predicate} on I that filters only the items to match
     * @param <I>
     *            The type of the input {@link Iterable}
     * @param <O>
     *            The type of the output {@link Iterable}
     * @return The {@link Iterable} of O
     */
    public static <I, O> Iterable<O> filterTranslate(final Iterable<I> input,
            final Function<I, O> converter, final Predicate<I> matcher)
    {
        return new Iterable<O>()
        {
            @Override
            public Iterator<O> iterator()
            {
                return new Iterator<O>()
                {
                    private boolean consumed = true;
                    private final Iterator<I> iterator = input.iterator();
                    private I next = null;
                    private boolean valid = false;

                    @Override
                    public boolean hasNext()
                    {
                        if (this.consumed)
                        {
                            this.next = null;
                            this.valid = false;
                            while (this.iterator.hasNext() && !this.valid)
                            {
                                this.next = this.iterator.next();
                                this.valid = matcher.test(this.next);
                            }
                            this.consumed = false;
                        }
                        return this.valid;
                    }

                    @Override
                    public O next()
                    {
                        if (hasNext())
                        {
                            this.consumed = true;
                            return converter.apply(this.next);
                        }
                        throw new NoSuchElementException();
                    }
                };
            }

            @SuppressWarnings("unused")
            public void useless()
            {
                // Unused
            }
        };
    }

    /**
     * Get the first element of an {@link Iterable}
     *
     * @param types
     *            The items
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The first element in the {@link Iterable}, or empty if none.
     */
    public static <T> Optional<T> first(final Iterable<T> types)
    {
        return nth(types, 0);
    }

    public static <T> Optional<T> firstMatching(final Iterable<T> types, final Predicate<T> matcher)
    {
        return first(filter(types, matcher));
    }

    /**
     * Create an {@link Iterable} from an {@link Enumeration}
     *
     * @param types
     *            The {@link Enumeration}
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <T> Iterable<T> from(final Enumeration<T> types)
    {
        return () -> new Iterator<T>()
        {
            @Override
            public boolean hasNext()
            {
                return types.hasMoreElements();
            }

            @Override
            public T next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }
                return types.nextElement();
            }
        };
    }

    /**
     * Create an {@link Iterable} from 0 to many items of the provided type
     *
     * @param types
     *            The 0 to many array of items to include
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    @SafeVarargs
    public static <T> Iterable<T> from(final T... types)
    {
        return asList(types);
    }

    /**
     * Get the head (first) element of an {@link Iterable}
     *
     * @param types
     *            The items
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The head element in the {@link Iterable}, or null if none.
     */
    public static <T> T head(final Iterable<T> types)
    {
        final Iterator<T> iterator = types.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Get an {@link Iterable} based on something that can return the value at a specific index.
     *
     * @param size
     *            The total size of the collection
     * @param supplier
     *            The provider of the value based on the index
     * @param <T>
     *            The type to return within the {@link Iterable}
     * @return The index based {@link Iterable}
     */
    public static <T> Iterable<T> indexBasedIterable(final long size,
            final LongFunction<T> supplier)
    {
        return () -> new Iterator<T>()
        {
            private long index = 0L;

            @Override
            public boolean hasNext()
            {
                return this.index < size;
            }

            @Override
            public T next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }
                return supplier.apply(this.index++);
            }
        };
    }

    /**
     * Determines if the given iterable is empty
     *
     * @param types
     *            The iterable to check
     * @return {@code true} if the iterable contains no elements
     */
    public static boolean isEmpty(final Iterable<?> types)
    {
        if (types instanceof Collection)
        {
            return ((Collection<?>) types).isEmpty();
        }
        return !types.iterator().hasNext();
    }

    /**
     * Translate a passed array of Items to an {@link Iterable} of Items
     *
     * @param types
     *            The items
     * @param <T>
     *            The type of the {@link Iterable}
     * @return An {@link Iterable} of items.
     */
    public static <T> Iterable<T> iterable(@SuppressWarnings("unchecked") final T... types)
    {
        return indexBasedIterable(types.length, index -> types[(int) index]);
    }

    /**
     * Build an new Iterable by prepending the head element to the tail iterable.
     *
     * @param head
     *            The item to place in the head position
     * @param tail
     *            The items positioned after the head
     * @param <T>
     *            The type of the head and tail {@link Iterable}
     * @return An {@link Iterable}
     */
    public static <T> Iterable<T> join(final T head, final Iterable<T> tail)
    {
        return () -> new Iterator<T>()
        {
            private final Iterator<T> tailIterator = tail.iterator();
            private boolean headConsumed = false;

            @Override
            public boolean hasNext()
            {
                return !this.headConsumed || this.tailIterator.hasNext();
            }

            @Override
            public T next()
            {
                if (this.headConsumed)
                {
                    return this.tailIterator.next();
                }
                this.headConsumed = true;
                return head;
            }
        };
    }

    /**
     * Get the last element of an {@link Iterable}
     *
     * @param types
     *            The items
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The last element in the {@link Iterable}
     */
    public static <T> Optional<T> last(final Iterable<T> types)
    {
        T result = null;
        if (types instanceof List)
        {
            final List<T> list = (List<T>) types;
            if (!list.isEmpty())
            {
                result = list.get(list.size() - 1);
            }
        }
        else
        {
            for (final T type : types)
            {
                result = type;
            }
        }
        return Optional.ofNullable(result);
    }

    public static <T> Optional<T> lastMatching(final Iterable<T> types, final Predicate<T> matcher)
    {
        return last(filter(types, matcher));
    }

    /**
     * Get the nth element of an {@link Iterable}
     *
     * @param types
     *            The items
     * @param index
     *            The index at which to pick
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The first element in the {@link Iterable}, or empty if the iterable has no element at
     *         this index.
     */
    public static <T> Optional<T> nth(final Iterable<T> types, final long index)
    {
        long counter = 0L;
        final Iterator<T> iterator = types.iterator();
        T result = iterator.hasNext() ? iterator.next() : null;
        while (counter++ < index)
        {
            if (iterator.hasNext())
            {
                result = iterator.next();
            }
            else
            {
                result = null;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    /**
     * Create a {@link StreamIterable} that uses parallelization
     *
     * @param source
     *            The {@link Iterable} to use as source
     * @param <T>
     *            The type of the source {@link Iterable}
     * @return The corresponding {@link StreamIterable}
     */
    public static <T> StreamIterable<T> parallelStream(final Iterable<T> source)
    {
        return new StreamIterable<>(source, true);
    }

    public static <T> void print(final Iterable<T> input, final String name)
    {
        System.out.println(toString(input, name)); // NOSONAR
    }

    /**
     * Iterate over an {@link Iterable} to get its size. If the {@link Iterable} is a sub instance
     * of {@link Collection}, then it reads the size from it directly; it will not iterate
     * unnecessarily.
     *
     * @param <T>
     *            The type of the {@link Iterable}
     * @param types
     *            The input {@link Iterable}
     * @return The size of the {@link Iterable}
     */
    public static <T> long size(final Iterable<T> types)
    {
        if (types instanceof Collection)
        {
            return ((Collection<T>) types).size();
        }
        return count(types, type -> 1L);
    }

    /**
     * Create a {@link StreamIterable}
     *
     * @param source
     *            The {@link Iterable} to use as source
     * @param <T>
     *            The type of the source {@link Iterable}
     * @return The corresponding {@link StreamIterable}
     */
    public static <T> StreamIterable<T> stream(final Iterable<T> source)
    {
        return new StreamIterable<>(source);
    }

    /**
     * Get an {@link Iterable} of all elements beyond the head.
     *
     * @param types
     *            The items
     * @param <T>
     *            The type of the {@link Iterable}
     * @return An {@link Iterable}
     */
    public static <T> Iterable<T> tail(final Iterable<T> types)
    {
        final Iterator<T> iterator = types.iterator();
        if (iterator.hasNext())
        {
            iterator.next();
        }
        return () -> iterator;
    }

    /**
     * Translate an array to an {@link List}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    @SafeVarargs
    public static <T> List<T> toList(final T... types)
    {
        return asList(types);
    }

    /**
     * Translate an {@link Iterable} to a {@link String}
     *
     * @param input
     *            The input {@link Iterable}
     * @param <T>
     *            The type of the {@link Iterable}
     * @param name
     *            The name of the input {@link Iterable}
     * @return A {@link String} representation of the {@link Iterable}
     */
    public static <T> String toString(final Iterable<T> input, final String name)
    {
        return toString(input, name, ", ");
    }

    /**
     * Translate an {@link Iterable} to a {@link String}
     *
     * @param input
     *            The input {@link Iterable}
     * @param <T>
     *            The type of the {@link Iterable}
     * @param name
     *            The name of the input {@link Iterable}
     * @param separator
     *            The separator to use between each item in the input {@link Iterable}
     * @return A {@link String} representation of the {@link Iterable}
     */
    public static <T> String toString(final Iterable<T> input, final String name,
            final String separator)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(name);
        builder.append(": ");
        long index = 0;
        for (final T type : input)
        {
            if (index > 0)
            {
                builder.append(separator);
            }
            builder.append(type.toString());
            index++;
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Translate an {@link Iterable} of type I to an {@link Iterable} of type O.
     *
     * @param input
     *            The input {@link Iterable}
     * @param converter
     *            The converter from I to O
     * @param <I>
     *            The type of the input {@link Iterable}
     * @param <O>
     *            The type of the output {@link Iterable}
     * @return The {@link Iterable} of O
     */
    public static <I, O> Iterable<O> translate(final Iterable<I> input,
            final Function<I, O> converter)
    {
        return filterTranslate(input, converter, item -> true);
    }

    /**
     * Translate an {@link Iterable} of type I to an {@link Iterable} of type O.
     *
     * @param input
     *            The input {@link Iterable}
     * @param converter
     *            The converter from I to O
     * @param <I>
     *            The type of the input {@link Iterable}
     * @param <O>
     *            The type of the output {@link Iterable}
     * @param matcher
     *            A {@link Predicate} on O that filters only the items to match
     * @return The {@link Iterable} of O
     */
    public static <I, O> Iterable<O> translateFilter(final Iterable<I> input,
            final Function<I, O> converter, final Predicate<O> matcher)
    {
        return Iterables.filter(Iterables.translate(input, converter), matcher);
    }

    /**
     * Translate an {@link Iterable} of type I to an {@link Iterable} of O where each converter
     * yields multiple O for each I.
     *
     * @param iterableIn
     *            The input {@link Iterable}
     * @param converter
     *            The converter from I to multiple O
     * @param <I>
     *            The type of the input {@link Iterable}
     * @param <O>
     *            The type of the output {@link Iterable}
     * @return The {@link Iterable} of O
     */
    public static <I, O> Iterable<O> translateMulti(final Iterable<I> iterableIn,
            final Function<I, Iterable<? extends O>> converter)
    {
        return new MultiIterable<>(Iterables.translate(iterableIn, converter));
    }

    /**
     * Truncate an {@link Iterable}.
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param startIndex
     *            The index before which to truncate from the start
     * @param indexFromEnd
     *            The index after which to truncate from the end
     * @param <T>
     *            The type of the {@link Iterable}
     * @return The truncated {@link Iterable}
     */
    public static <T> Iterable<T> truncate(final Iterable<T> types, final int startIndex,
            final int indexFromEnd)
    {
        return new SubIterable<>(types, startIndex, indexFromEnd);
    }

    private Iterables()
    {
    }
}

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
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;

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
     * @param <Type>
     *            what kind of objects we're copying
     * @return true if addHere has changed, false otherwise
     */
    public static <Type> boolean addAll(final Collection<Type> addHere, final Iterable<Type> from)
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <Type> Iterable<Type> asIterable(final Iterable<Type> types)
    {
        return () -> types.iterator();
    }

    /**
     * Translate an {@link Iterable} into a {@link List}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <Type> List<Type> asList(final Iterable<Type> types)
    {
        if (types instanceof List)
        {
            return (List<Type>) types;
        }
        final List<Type> result = new ArrayList<>();
        types.forEach(type ->
        {
            result.add(type);
        });
        return result;
    }

    /**
     * Translate an array to an {@link List}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <Type> List<Type> asList(final Type[] types)
    {
        final List<Type> result = new ArrayList<>();
        for (final Type type : types)
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
     * @param <TypeKey>
     *            The type of key of the entry
     * @param <TypeValue>
     *            The type of value of the entry
     * @return The translated {@link Iterable}
     */
    public static <TypeKey, TypeValue> Map<TypeKey, TypeValue> asMap(
            final Iterable<Map.Entry<TypeKey, TypeValue>> types)
    {
        final Map<TypeKey, TypeValue> result = new HashMap<>();
        types.forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    /**
     * Translate an {@link Iterable} into a {@link Queue}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <Type> Queue<Type> asQueue(final Iterable<Type> types)
    {
        final Queue<Type> result = new LinkedList<>();
        types.forEach(type ->
        {
            result.add(type);
        });
        return result;
    }

    /**
     * Translate an {@link Iterable} into a {@link Set}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <Type> Set<Type> asSet(final Iterable<Type> types)
    {
        final Set<Type> result = new HashSet<>();
        types.forEach(type ->
        {
            result.add(type);
        });
        return result;
    }

    /**
     * Translate an array to an {@link Set}
     *
     * @param types
     *            The {@link Iterable} to translate
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <Type> Set<Type> asSet(final Type[] types)
    {
        final Set<Type> result = new HashSet<>();
        for (final Type type : types)
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <Type> SortedSet<Type> asSortedSet(final Iterable<Type> types)
    {
        final SortedSet<Type> result = new TreeSet<>();
        types.forEach(type ->
        {
            result.add(type);
        });
        return result;
    }

    /**
     * Test if an {@link Iterable} iterates at some point on an item.
     *
     * @param types
     *            The {@link Iterable} to test
     * @param type
     *            The item to test
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return True if the {@link Iterable} iterates at some point on the item.
     */
    public static <Type> boolean contains(final Iterable<Type> types, final Type type)
    {
        if (types instanceof Collection)
        {
            return ((Collection<Type>) types).contains(type);
        }
        for (final Type candidate : types)
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The total count
     */
    public static <Type> long count(final Iterable<Type> types,
            final Function<Type, Long> typeCounter)
    {
        long result = 0;
        for (final Type type : types)
        {
            result += typeCounter.apply(type);
        }
        return result;
    }

    /**
     * @param example
     *            A random object to specify the type
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return An empty {@link Iterable} of the right type
     */
    public static <Type> Iterable<Type> emptyIterable(final Type example)
    {
        return () -> new Iterator<Type>()
        {
            @Override
            public boolean hasNext()
            {
                return false;
            }

            @Override
            public Type next()
            {
                return null;
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return True if the two {@link Iterable}s iterate on the same items.
     */
    public static <Type> boolean equals(final Iterable<Type> that, final Iterable<Type> other)
    {
        // Handle null iterables
        // If they are both null, then equal
        // If only one of them is null, then NOT equal
        final boolean thatIsNull = that == null;
        final boolean otherIsNull = other == null;
        if (thatIsNull || otherIsNull)
        {
            return thatIsNull & thatIsNull;
        }

        // Iterables are not null, let's check for size first
        // Then the values
        final long thatSize = Iterables.size(that);
        if (thatSize != Iterables.size(other))
        {
            return false;
        }
        final Iterator<Type> thatIterator = that.iterator();
        final Iterator<Type> otherIterator = other.iterator();
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The filtered {@link Iterable}
     */
    public static <Type> Iterable<Type> filter(final Iterable<Type> input,
            final Predicate<Type> matcher)
    {
        return filterTranslate(input, item -> item, matcher);
    }

    /**
     * Translate an {@link Iterable} of type TypeIn to an {@link Iterable} of type TypeOut.
     *
     * @param input
     *            The input {@link Iterable}
     * @param converter
     *            The converter from TypeIn to TypeOut
     * @param matcher
     *            A {@link Predicate} on TypeIn that filters only the items to match
     * @param <TypeIn>
     *            The type of the input {@link Iterable}
     * @param <TypeOut>
     *            The type of the output {@link Iterable}
     * @return The {@link Iterable} of TypeOut
     */
    public static <TypeIn, TypeOut> Iterable<TypeOut> filterTranslate(final Iterable<TypeIn> input,
            final Function<TypeIn, TypeOut> converter, final Predicate<TypeIn> matcher)
    {
        return new Iterable<TypeOut>()
        {
            @Override
            public Iterator<TypeOut> iterator()
            {
                return new Iterator<TypeOut>()
                {
                    private boolean consumed = true;
                    private final Iterator<TypeIn> iterator = input.iterator();
                    private TypeIn next = null;
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
                    public TypeOut next()
                    {
                        if (hasNext())
                        {
                            this.consumed = true;
                            return converter.apply(this.next);
                        }
                        return null;
                    }
                };
            }

            @SuppressWarnings("unused")
            public void useless()
            {
            }
        };
    }

    /**
     * Get the first element of an {@link Iterable}
     *
     * @param types
     *            The items
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The first element in the {@link Iterable}, or empty if none.
     */
    public static <Type> Optional<Type> first(final Iterable<Type> types)
    {
        return nth(types, 0);
    }

    /**
     * Create an {@link Iterable} from an {@link Enumeration}
     *
     * @param types
     *            The {@link Enumeration}
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    public static <Type> Iterable<Type> from(final Enumeration<Type> types)
    {
        return () -> new Iterator<Type>()
        {
            @Override
            public boolean hasNext()
            {
                return types.hasMoreElements();
            }

            @Override
            public Type next()
            {
                return types.nextElement();
            }
        };
    }

    /**
     * Create an {@link Iterable} from 0 to many items of the provided type
     *
     * @param types
     *            The 0 to many array of items to include
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    @SafeVarargs
    public static <Type> Iterable<Type> from(final Type... types)
    {
        return asList(types);
    }

    /**
     * Get the head (first) element of an {@link Iterable}
     *
     * @param types
     *            The items
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The head element in the {@link Iterable}, or null if none.
     */
    public static <Type> Type head(final Iterable<Type> types)
    {
        final Iterator<Type> iterator = types.iterator();
        return iterator.hasNext() ? iterator.next() : null;
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return An {@link Iterable} of items.
     */
    public static <Type> Iterable<Type> iterable(@SuppressWarnings("unchecked") final Type... types)
    {
        return () -> new Iterator<Type>()
        {
            private int index = 0;
            private final int max = types.length;

            @Override
            public boolean hasNext()
            {
                return this.index < this.max;
            }

            @Override
            public Type next()
            {
                if (this.index < this.max)
                {
                    return types[this.index++];
                }
                return null;
            }
        };
    }

    /**
     * Build an new Iterable by prepending the head element to the tail iterable.
     *
     * @param head
     *            The item to place in the head position
     * @param tail
     *            The items positioned after the head
     * @param <Type>
     *            The type of the head and tail {@link Iterable}
     * @return An {@link Iterable}
     */
    public static <Type> Iterable<Type> join(final Type head, final Iterable<Type> tail)
    {
        return () -> new Iterator<Type>()
        {
            private final Iterator<Type> tailIterator = tail.iterator();
            private boolean headConsumed = false;

            @Override
            public boolean hasNext()
            {
                return !this.headConsumed || this.tailIterator.hasNext();
            }

            @Override
            public Type next()
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The last element in the {@link Iterable}
     */
    public static <Type> Optional<Type> last(final Iterable<Type> types)
    {
        final List<Type> list = asList(types);
        if (list.size() >= 1)
        {
            return Optional.ofNullable(list.get(list.size() - 1));
        }
        else
        {
            throw new CoreException("No last item when there is nothing.");
        }
    }

    /**
     * Get the nth element of an {@link Iterable}
     *
     * @param types
     *            The items
     * @param index
     *            The index at which to pick
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The first element in the {@link Iterable}, or empty if the iterable has no element at
     *         this index.
     */
    public static <Type> Optional<Type> nth(final Iterable<Type> types, final long index)
    {
        long counter = 0L;
        final Iterator<Type> iterator = types.iterator();
        Type result = iterator.hasNext() ? iterator.next() : null;
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

    public static <T> void print(final Iterable<T> input, final String name)
    {
        System.out.println(toString(input, name));
    }

    /**
     * Iterate over an {@link Iterable} to get its size. If the {@link Iterable} is a sub instance
     * of {@link Collection}, then it reads the size from it directly; it will not iterate
     * unnecessarily.
     *
     * @param <Type>
     *            The type of the {@link Iterable}
     * @param types
     *            The input {@link Iterable}
     * @return The size of the {@link Iterable}
     */
    public static <Type> long size(final Iterable<Type> types)
    {
        if (types instanceof Collection)
        {
            return ((Collection<Type>) types).size();
        }
        return count(types, type -> 1L);
    }

    /**
     * Create a {@link StreamIterable}
     *
     * @param source
     *            The {@link Iterable} to use as source
     * @param <Type>
     *            The type of the source {@link Iterable}
     * @return The corresponding {@link StreamIterable}
     */
    public static <Type> StreamIterable<Type> stream(final Iterable<Type> source)
    {
        return new StreamIterable<>(source);
    }

    /**
     * Get an {@link Iterable} of all elements beyond the head.
     *
     * @param types
     *            The items
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return An {@link Iterable}
     */
    public static <Type> Iterable<Type> tail(final Iterable<Type> types)
    {
        final Iterator<Type> iterator = types.iterator();
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The translated {@link Iterable}
     */
    @SafeVarargs
    public static <Type> List<Type> toList(final Type... types)
    {
        return asList(types);
    }

    /**
     * Translate an {@link Iterable} to a {@link String}
     *
     * @param input
     *            The input {@link Iterable}
     * @param <Type>
     *            The type of the {@link Iterable}
     * @param name
     *            The name of the input {@link Iterable}
     * @return A {@link String} representation of the {@link Iterable}
     */
    public static <Type> String toString(final Iterable<Type> input, final String name)
    {
        return toString(input, name, ", ");
    }

    /**
     * Translate an {@link Iterable} to a {@link String}
     *
     * @param input
     *            The input {@link Iterable}
     * @param <Type>
     *            The type of the {@link Iterable}
     * @param name
     *            The name of the input {@link Iterable}
     * @param separator
     *            The separator to use between each item in the input {@link Iterable}
     * @return A {@link String} representation of the {@link Iterable}
     */
    public static <Type> String toString(final Iterable<Type> input, final String name,
            final String separator)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(name);
        builder.append(": ");
        long index = 0;
        for (final Type type : input)
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
     * Translate an {@link Iterable} of type TypeIn to an {@link Iterable} of type TypeOut.
     *
     * @param input
     *            The input {@link Iterable}
     * @param converter
     *            The converter from TypeIn to TypeOut
     * @param <TypeIn>
     *            The type of the input {@link Iterable}
     * @param <TypeOut>
     *            The type of the output {@link Iterable}
     * @return The {@link Iterable} of TypeOut
     */
    public static <TypeIn, TypeOut> Iterable<TypeOut> translate(final Iterable<TypeIn> input,
            final Function<TypeIn, TypeOut> converter)
    {
        return filterTranslate(input, converter, item -> true);
    }

    /**
     * Translate an {@link Iterable} of type TypeIn to an {@link Iterable} of type TypeOut.
     *
     * @param input
     *            The input {@link Iterable}
     * @param converter
     *            The converter from TypeIn to TypeOut
     * @param <TypeIn>
     *            The type of the input {@link Iterable}
     * @param <TypeOut>
     *            The type of the output {@link Iterable}
     * @param matcher
     *            A {@link Predicate} on TypeOut that filters only the items to match
     * @return The {@link Iterable} of TypeOut
     */
    public static <TypeIn, TypeOut> Iterable<TypeOut> translateFilter(final Iterable<TypeIn> input,
            final Function<TypeIn, TypeOut> converter, final Predicate<TypeOut> matcher)
    {
        return Iterables.filter(Iterables.translate(input, converter), matcher);
    }

    /**
     * Translate an {@link Iterable} of type TypeIn to an {@link Iterable} of TypeOut where each
     * converter yields multiple TypeOut for each TypeIn.
     *
     * @param iterableIn
     *            The input {@link Iterable}
     * @param converter
     *            The converter from TypeIn to multiple TypeOut
     * @param <TypeIn>
     *            The type of the input {@link Iterable}
     * @param <TypeOut>
     *            The type of the output {@link Iterable}
     * @return The {@link Iterable} of TypeOut
     */
    public static <TypeIn, TypeOut> Iterable<TypeOut> translateMulti(
            final Iterable<TypeIn> iterableIn,
            final Function<TypeIn, Iterable<? extends TypeOut>> converter)
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
     * @param <Type>
     *            The type of the {@link Iterable}
     * @return The truncated {@link Iterable}
     */
    public static <Type> Iterable<Type> truncate(final Iterable<Type> types, final int startIndex,
            final int indexFromEnd)
    {
        return new SubIterable<>(types, startIndex, indexFromEnd);
    }

    private Iterables()
    {
    }
}

package org.openstreetmap.atlas.utilities.collections;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

/**
 * Takes an iterable and adds in a set of elements to skip over. Useful for when an iterable will be
 * iterated over multiple times, but some elements can be skipped for efficiency.
 *
 * @author samuelgass
 * @param <Type>
 *            the type of the {@link Iterable}
 * @param <IdentifierType>
 *            the type for the identifier used by the elements of Type for the {@link Iterable}
 */
public class FilteredIterable<Type, IdentifierType> implements Iterable<Type>
{
    private final Iterable<Type> source;
    private final Set<IdentifierType> filterSet;
    private final Function<Type, IdentifierType> identifier;

    /**
     * Constructor for FilteredIterable.
     *
     * @param source
     *            A source iterable to translate to FilteredIterable
     * @param filterSet
     *            A set of identifiers for elements to skip (can be empty or have members)
     * @param identifier
     *            A function that takes an element of Type for the {@link Iterable} and returns the
     *            identifier for that element
     */
    public FilteredIterable(final Iterable<Type> source, final Set<IdentifierType> filterSet,
            final Function<Type, IdentifierType> identifier)
    {
        this.filterSet = filterSet;
        this.source = source;
        this.identifier = identifier;
    }

    /**
     * Takes an element and uses the identifier function to add its identifier to the filter set.
     *
     * @param type
     *            The element to add to the filter set
     * @return True if an element was added to the filter set, false if it wasn't (likely in the
     *         case it was already present in the set)
     */
    public boolean addToFilteredSet(final Type type)
    {
        return this.filterSet.add(this.identifier.apply(type));
    }

    @Override
    public Iterator<Type> iterator()
    {
        return new Iterator<Type>()
        {
            private final Iterator<Type> sourceIterator = FilteredIterable.this.source.iterator();
            private Type next = null;
            private Type current = this.next();

            @Override
            public boolean hasNext()
            {
                return this.next != null && !FilteredIterable.this.filterSet
                        .contains(FilteredIterable.this.identifier.apply(this.next));
            }

            @Override
            public Type next()
            {
                this.current = this.next;
                this.next = null;
                while (this.sourceIterator.hasNext())
                {
                    final Type nextCandidate = this.sourceIterator.next();
                    if (FilteredIterable.this.filterSet
                            .contains(FilteredIterable.this.identifier.apply(nextCandidate)))
                    {
                        continue;
                    }
                    else
                    {
                        this.next = nextCandidate;
                        break;
                    }
                }
                return this.current;
            }
        };
    }
}

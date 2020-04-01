package org.openstreetmap.atlas.utilities.collections;

import java.util.Iterator;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Iterator made of multiple sub-iterators
 *
 * @author matthieun
 * @param <T>
 *            The type of all the iterators
 */
public class MultiIterable<T> implements Iterable<T>
{
    private final Iterable<? extends Iterable<? extends T>> iterables;

    public MultiIterable(final Iterable<? extends Iterable<? extends T>> iterables)
    {
        this.iterables = iterables;
    }

    @SafeVarargs
    public MultiIterable(final Iterable<? extends T>... iterables)
    {
        if (iterables.length == 0)
        {
            throw new CoreException("Cannot have an empty set of Iterables.");
        }
        this.iterables = Iterables.asList(iterables);
    }

    @Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            private final Iterator<? extends Iterable<? extends T>> iterablesIterator = MultiIterable.this.iterables
                    .iterator();
            private Iterator<? extends T> currentIterator = this.iterablesIterator.hasNext()
                    ? this.iterablesIterator.next().iterator()
                    : null;

            @Override
            public boolean hasNext()
            {
                if (this.currentIterator == null)
                {
                    return false;
                }
                if (this.currentIterator != null && this.currentIterator.hasNext())
                {
                    return true;
                }
                while (this.iterablesIterator.hasNext())
                {
                    this.currentIterator = this.iterablesIterator.next().iterator();
                    if (this.currentIterator != null && this.currentIterator.hasNext())
                    {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public T next()
            {
                if (hasNext())
                {
                    return this.currentIterator.next();
                }
                return null;
            }
        };
    }
}

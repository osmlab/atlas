package org.openstreetmap.atlas.utilities.collections;

import java.util.Iterator;
import java.util.Optional;

/**
 * Iterator that will skip over the empty {@link Optional}s
 *
 * @param <T>
 *            The type of the {@link Optional} iterator
 * @author cuthbertm
 * @author jklamer
 */
public class OptionalIterable<T> implements Iterable<T>
{
    private final Iterable<Optional<T>> iterable;

    public OptionalIterable(final Iterable<Optional<T>> iterable)
    {
        this.iterable = iterable;
    }

    @Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            private final Iterator<Optional<T>> iterator = OptionalIterable.this.iterable
                    .iterator();
            private Optional<T> previousElement = Optional.empty();

            @Override
            public boolean hasNext()
            {
                if (previousElement.isPresent())
                {
                    return true;
                }
                else
                {
                    while (this.iterator.hasNext())
                    {
                        final Optional<T> current = this.iterator.next();
                        if (current.isPresent())
                        {
                            this.previousElement = current;
                            return true;
                        }
                    }
                    return false;
                }
            }

            @Override
            public T next()
            {
                if (this.previousElement.isPresent())
                {
                    final T returnElement = this.previousElement.get();
                    this.previousElement = Optional.empty();
                    return returnElement;
                }
                else
                {
                    while (this.iterator.hasNext())
                    {
                        final Optional<T> item = this.iterator.next();
                        if (item.isPresent())
                        {
                            return item.get();
                        }
                    }
                    return null;
                }
            }
        };
    }
}

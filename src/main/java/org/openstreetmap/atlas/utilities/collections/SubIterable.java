package org.openstreetmap.atlas.utilities.collections;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Takes an {@link Iterable} and truncates some items at the beginning and some items at the end.
 *
 * @author matthieun
 * @param <Type>
 *            The type of the {@link Iterable}
 */
public class SubIterable<Type> implements Iterable<Type>
{
    private final Iterable<Type> source;
    private final int startIndex;
    private final int indexFromEnd;

    public SubIterable(final Iterable<Type> source, final int startIndex, final int indexFromEnd)
    {
        this.source = source;
        this.startIndex = startIndex > 0 ? startIndex : 0;
        this.indexFromEnd = indexFromEnd > 0 ? indexFromEnd : 0;
    }

    @Override
    public Iterator<Type> iterator()
    {
        return new Iterator<Type>()
        {
            private int index = 0;
            private final Iterator<Type> sourceIterator = SubIterable.this.source.iterator();
            private final Queue<Type> lookAheadStore = new LinkedList<>();

            @Override
            public boolean hasNext()
            {
                while (this.index < SubIterable.this.startIndex)
                {
                    if (this.sourceIterator.hasNext())
                    {
                        this.sourceIterator.next();
                    }
                    this.index++;
                }
                while (this.sourceIterator.hasNext()
                        && this.lookAheadStore.size() < SubIterable.this.indexFromEnd)
                {
                    this.lookAheadStore.add(this.sourceIterator.next());
                }
                if (this.lookAheadStore.size() == SubIterable.this.indexFromEnd)
                {
                    return this.sourceIterator.hasNext();
                }
                else
                {
                    return false;
                }
            }

            @Override
            public Type next()
            {
                if (hasNext())
                {
                    final Type result = this.lookAheadStore.isEmpty() ? this.sourceIterator.next()
                            : this.lookAheadStore.poll();
                    this.index++;
                    return result;
                }
                else
                {
                    return null;
                }
            }
        };
    }
}

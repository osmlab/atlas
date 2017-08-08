package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This allows you iterator in parallel over multiple iterators. So simplest example would be 2 int
 * iterators {1, 2} and {3, 4}. First iteration would return {1, 3}, second iteration would return
 * {2, 4}. The Iterable can handle different list types (eg. {1, 2} and {"one", "two"}) and can
 * handle varying lengths (eg. {1, 2} and {1, 2, 3}). In the example of varying lengths when
 * retrieving the third iterations elements it will return null or Optional.empty(), depending on
 * how you request it, for the 1st list.
 *
 * @author cuthbertm
 */
@SuppressWarnings("rawtypes")
public class ParallelIterable implements Iterable<JoinedCollection>
{
    private final List<Iterable> iterables;

    public ParallelIterable(final Iterable... iterables)
    {
        this.iterables = Arrays.asList(iterables);
    }

    public List<Iterator> getIteratorList()
    {
        final List<Iterator> iteratorList = new ArrayList<>();
        this.iterables.forEach(iterator -> iteratorList.add(iterator.iterator()));
        return iteratorList;
    }

    @Override
    public Iterator<JoinedCollection> iterator()
    {
        return new Iterator<JoinedCollection>()
        {
            private final List<Iterator> iterators = getIteratorList();

            @Override
            public boolean hasNext()
            {
                // this strange line will filter out any of the iterables that do not have any more
                // elements, and then we will check to see if the result has any more elements.
                return Iterables.filter(this.iterators, Iterator::hasNext).iterator().hasNext();
            }

            @Override
            public JoinedCollection next()
            {
                final JoinedCollection joined = new JoinedCollection(this.iterators.size());
                for (int index = 0; index < this.iterators.size(); index++)
                {
                    final Iterator iterator = this.iterators.get(index);
                    if (iterator.hasNext())
                    {
                        joined.set(index, iterator.next());
                    }
                }
                return joined;
            }
        };
    }
}

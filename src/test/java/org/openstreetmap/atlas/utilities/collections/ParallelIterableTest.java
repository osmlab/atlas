package org.openstreetmap.atlas.utilities.collections;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author cuthbertm
 */
public class ParallelIterableTest
{
    @Test
    public void testDifferentObjectTypes()
    {
        final List<Long> firstThree = Arrays.asList(1L, 2L, 3L);
        final List<String> secondThree = Arrays.asList("test1", "test2", "test3");
        final List<Integer> thirdThree = Arrays.asList(7, 8, 9);
        final ParallelIterable parallel = new ParallelIterable(firstThree, secondThree, thirdThree);
        final Iterator<JoinedCollection> iterator = parallel.iterator();
        int indexCounter = 0;
        while (iterator.hasNext())
        {
            final JoinedCollection joined = iterator.next();
            Assert.assertEquals(firstThree.get(indexCounter), joined.get(0));
            Assert.assertEquals(secondThree.get(indexCounter), joined.get(1));
            Assert.assertEquals(thirdThree.get(indexCounter++), joined.get(2));
        }
    }

    @Test
    public void testMultipleLongTypes()
    {
        final List<Long> firstThree = Arrays.asList(1L, 2L, 3L);
        final List<Long> secondThree = Arrays.asList(4L, 5L, 6L);
        final List<Long> thirdThree = Arrays.asList(7L, 8L, 9L);
        final ParallelIterable parallel = new ParallelIterable(firstThree, secondThree, thirdThree);
        final Iterator<JoinedCollection> iterator = parallel.iterator();
        int indexCounter = 0;
        while (iterator.hasNext())
        {
            final JoinedCollection joined = iterator.next();
            Assert.assertEquals(firstThree.get(indexCounter), joined.get(0));
            Assert.assertEquals(secondThree.get(indexCounter), joined.get(1));
            Assert.assertEquals(thirdThree.get(indexCounter++), joined.get(2));
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUnevenLists()
    {
        final List<Long> firstThree = Arrays.asList(1L, 2L, 3L, 12L, 45L);
        final List<Long> secondThree = Arrays.asList(4L, 5L, 6L);
        final List<Long> thirdThree = Arrays.asList(7L, 8L, 9L, 10L);
        final ParallelIterable parallel = new ParallelIterable(firstThree, secondThree, thirdThree);
        final Iterator<JoinedCollection> iterator = parallel.iterator();
        int indexCounter = 0;
        while (iterator.hasNext())
        {
            final JoinedCollection joined = iterator.next();
            Assert.assertEquals(Optional.of(firstThree.get(indexCounter)), joined.getOption(0));
            if (indexCounter > 2)
            {
                Assert.assertEquals(Optional.empty(), joined.getOption(1));
            }
            else
            {
                Assert.assertEquals(Optional.of(secondThree.get(indexCounter)),
                        joined.getOption(1));
            }
            if (indexCounter > 3)
            {
                Assert.assertEquals(null, joined.get(2));
            }
            else
            {
                Assert.assertEquals(Optional.of(thirdThree.get(indexCounter++)),
                        joined.getOption(2));
            }
        }
    }
}

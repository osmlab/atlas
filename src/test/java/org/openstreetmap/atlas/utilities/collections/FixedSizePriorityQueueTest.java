package org.openstreetmap.atlas.utilities.collections;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author tony
 */
public class FixedSizePriorityQueueTest
{
    @Test
    public void testComparable()
    {
        final Comparator<Integer> reversed = (first, second) -> first >= second ? -1 : 1;
        final FixedSizePriorityQueue<Integer> queue = new FixedSizePriorityQueue<>(1, reversed);

        queue.add(-1);
        queue.add(3);
        Assert.assertEquals("The size of queue should be 1", 1, queue.size());
        Assert.assertTrue("The queue should contain -1", queue.contains(-1));

        queue.add(-3);
        Assert.assertEquals("The size of queue should be 1", 1, queue.size());
        Assert.assertTrue("The queue should contain -3", queue.contains(-3));
    }

    @Test
    public void testSize()
    {
        final FixedSizePriorityQueue<Integer> queue = new FixedSizePriorityQueue<>(3);

        queue.add(5);
        queue.add(1);
        queue.add(2);
        Assert.assertEquals("The size of queue should be 3", 3, queue.size());
        Assert.assertTrue("The queue should contain 1", queue.contains(1));

        queue.add(6);
        Assert.assertEquals("The size of queue should still be 3", 3, queue.size());
        Assert.assertFalse("The queue should not contain 1", queue.contains(1));
        Assert.assertTrue("The queue should contain 6", queue.contains(6));
    }
}

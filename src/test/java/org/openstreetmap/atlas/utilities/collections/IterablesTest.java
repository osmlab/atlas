package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class IterablesTest
{
    @Test
    public void testCount()
    {
        final Iterable<Integer> iterable = () -> new Iterator<Integer>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return this.index < 100;
            }

            @Override
            public Integer next()
            {
                return this.index++;
            }
        };
        Assert.assertEquals(200, Iterables.count(iterable, integer -> 2L));

        final List<Integer> list = Iterables.asList(iterable);
        Assert.assertEquals(300, Iterables.count(list, integer -> 3L));
    }

    @Test
    public void testFilter()
    {
        final List<Integer> input = new ArrayList<>();
        input.add(1);
        input.add(null);
        input.add(3);
        final List<Integer> outputNonNull = Iterables
                .asList(Iterables.filter(input, in -> in != null));
        Assert.assertEquals(2, outputNonNull.size());
        final List<Integer> outputNull = Iterables
                .asList(Iterables.filter(input, in -> in == null));
        Assert.assertEquals(1, outputNull.size());
        final List<Integer> outputNotThree = Iterables
                .asList(Iterables.filter(input, in -> !new Integer(3).equals(in)));
        Assert.assertEquals(2, outputNotThree.size());
    }

    @Test
    public void testHead()
    {
        final List<Integer> input = new ArrayList<>();
        input.add(1);
        input.add(2);
        input.add(3);

        Assert.assertNotNull(Iterables.head(input));
        Assert.assertEquals(input.get(0), Iterables.head(input));
        Assert.assertNull(Iterables.head(Collections.emptyList()));
    }

    @Test
    public void testIsEmpty()
    {
        final List<Integer> input = new ArrayList<>();
        Assert.assertTrue("List is empty to start", Iterables.isEmpty(input));
        input.add(1);
        Assert.assertFalse("List is not empty", Iterables.isEmpty(input));
        input.remove(0);
        Assert.assertTrue("List is empty to end", Iterables.isEmpty(input));
    }

    @Test
    public void testJoin()
    {
        final List<Integer> rebuilt = Iterables.asList(
                Iterables.join(1, Iterables.join(2, Iterables.join(3, Collections.emptyList()))));

        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, rebuilt.toArray());
    }

    @Test
    public void testSize()
    {
        final Iterable<Integer> iterable = () -> new Iterator<Integer>()
        {
            private int index = 0;

            @Override
            public boolean hasNext()
            {
                return this.index < 100;
            }

            @Override
            public Integer next()
            {
                return this.index++;
            }
        };
        Assert.assertEquals(100, Iterables.size(iterable));
        final List<Integer> list = Iterables.asList(iterable);
        Assert.assertEquals(100, Iterables.size(list));
    }

    @Test
    public void testTail()
    {
        final List<Integer> input = new ArrayList<>();
        input.add(1);
        input.add(2);
        input.add(3);

        final List<Integer> tailOfTwo = Iterables.asList(Iterables.tail(input));
        Assert.assertArrayEquals(new Integer[] { 2, 3 }, tailOfTwo.toArray());

        final List<Integer> tailOfOne = Iterables.asList(Iterables.tail(tailOfTwo));
        Assert.assertArrayEquals(new Integer[] { 3 }, tailOfOne.toArray());

        final List<Integer> tailOfNone = Iterables.asList(Iterables.tail(tailOfOne));
        Assert.assertTrue(tailOfNone.isEmpty());

        final List<Integer> pastTail = Iterables.asList(Iterables.tail(tailOfNone));
        Assert.assertTrue(pastTail.isEmpty());
    }
}

package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class MultiIterableTest
{
    @Test
    public void multiIterableTest()
    {
        final List<Integer> list1 = new ArrayList<>();
        final List<Integer> list2 = new ArrayList<>();
        list1.add(1);
        list1.add(2);
        list2.add(3);
        list2.add(4);
        list2.add(5);

        final MultiIterable<Integer> multi = new MultiIterable<>(list1, list2);
        final Iterator<Integer> iterator = multi.iterator();
        Assert.assertEquals(Integer.valueOf(1), iterator.next());
        Assert.assertEquals(Integer.valueOf(2), iterator.next());
        Assert.assertEquals(Integer.valueOf(3), iterator.next());
        Assert.assertEquals(Integer.valueOf(4), iterator.next());
        Assert.assertEquals(Integer.valueOf(5), iterator.next());
        Assert.assertFalse(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }
}

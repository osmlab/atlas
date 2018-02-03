package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author cuthbertm
 */
public class OptionalIterableTest
{
    @Test
    public void iterationTest()
    {
        final List<Optional<Integer>> list = new ArrayList<>();
        list.add(Optional.of(1));
        list.add(Optional.of(2));
        list.add(Optional.empty());
        list.add(Optional.of(3));
        list.add(Optional.empty());
        list.add(Optional.empty());
        list.add(Optional.of(4));
        list.add(Optional.empty());

        final OptionalIterable<Integer> optionalIterable = new OptionalIterable<>(list);
        final Iterator<Integer> iterator = optionalIterable.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(Integer.valueOf(1), iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(Integer.valueOf(2), iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(Integer.valueOf(3), iterator.next());
        Assert.assertEquals(Integer.valueOf(4), iterator.next());
        Assert.assertFalse(iterator.hasNext());
        Assert.assertNull(iterator.next());
    }
}

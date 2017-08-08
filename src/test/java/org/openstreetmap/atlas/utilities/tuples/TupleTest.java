package org.openstreetmap.atlas.utilities.tuples;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author mgostintsev
 */
public class TupleTest
{
    public static final Tuple<Integer, Integer> getIntegerTuple()
    {
        final Tuple<Integer, Integer> tuple = Tuple.createTuple(1, 2);
        return tuple;
    }

    public static final Tuple<String, String> getStringTuple()
    {
        final Tuple<String, String> tuple = Tuple.createTuple("s1", "s2");
        return tuple;
    }

    @Test(expected = ClassCastException.class)
    public void testCastFailure()
    {
        final Tuple<?, ?> tuple = getIntegerTuple();
        Tuple.cast(tuple, String.class, String.class);
    }

    @Test
    public void testInstanceOf()
    {
        final Tuple<String, String> tuple = getStringTuple();
        Assert.assertTrue(tuple.isInstanceOf(String.class, String.class));
    }

    @Test
    public void testSuccessfulCast()
    {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final Tuple<?, ?> tuple = new Tuple("s1", "s2");

        final Tuple<String, String> castTuple = Tuple.cast(tuple, String.class, String.class);
        Assert.assertTrue(castTuple.isInstanceOf(String.class, String.class));
    }

    @Test
    public void testTuple()
    {
        final Tuple<String, String> tuple = getStringTuple();
        Assert.assertEquals(tuple.getFirst(), "s1");
        Assert.assertEquals(tuple.getSecond(), "s2");
    }
}

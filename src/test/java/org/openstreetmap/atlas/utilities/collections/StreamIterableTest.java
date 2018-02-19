package org.openstreetmap.atlas.utilities.collections;

import static java.util.Arrays.asList;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sagar Rohankar
 * @author mgostintsev
 */
public class StreamIterableTest
{
    @Test
    public void testAllMatch() throws Exception
    {
        final StreamIterable<Integer> streamIterable = new StreamIterable<>(asList(1, 2, 3, 4));

        // true -> all numbers are less than 5
        Assert.assertTrue(streamIterable.allMatch(n -> n < 5));
        // false -> all numbers are even
        Assert.assertFalse(streamIterable.allMatch(n -> n % 2 == 0));
    }

    @Test
    public void testAnyMatch()
    {
        final StreamIterable<Integer> streamIterable = new StreamIterable<>(asList(6, 12, 18));

        // true -> any number that is divisible by 9
        Assert.assertTrue(streamIterable.anyMatch(n -> n % 9 == 0));
        // false -> any number that is divisible by 5
        Assert.assertFalse(streamIterable.allMatch(n -> n % 5 == 0));
    }

}

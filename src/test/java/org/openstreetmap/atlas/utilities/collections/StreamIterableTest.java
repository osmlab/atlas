package org.openstreetmap.atlas.utilities.collections;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Sagar Rohankar
 */
public class StreamIterableTest
{

    @Test
    public void testAllMatch() throws Exception
    {
        final StreamIterable<Integer> streamIterable = new StreamIterable<>(asList(1, 2, 3, 4));

        // true -> all numbers are less than 5
        assertTrue(streamIterable.allMatch(n -> n < 5));
        // false -> all numbers are even
        assertFalse(streamIterable.allMatch(n -> n % 2 == 0));
    }

}

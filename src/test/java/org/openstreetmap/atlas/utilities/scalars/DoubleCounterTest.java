package org.openstreetmap.atlas.utilities.scalars;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for double counter
 *
 * @author jklamer
 */
public class DoubleCounterTest
{
    @Test
    public void testDoubleCounter()
    {
        final DoubleCounter myCount = new DoubleCounter(43.2);

        myCount.add(76.5);
        Assert.assertEquals(119.7, myCount.getValue(), 0);

        myCount.reset();
        final DoubleCounter otherCount = new DoubleCounter();

        Assert.assertEquals(myCount.getValue(), otherCount.getValue(), 0);
    }
}

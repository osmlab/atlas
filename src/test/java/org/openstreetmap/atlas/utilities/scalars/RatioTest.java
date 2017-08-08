package org.openstreetmap.atlas.utilities.scalars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author tony
 * @author matthieun
 */
public class RatioTest
{
    private static final double DELTA = 0.00001;

    @Test
    public void testPercentage()
    {
        final Ratio ratio = Ratio.percentage(15.51236632);
        assertTrue(ratio.isGreaterThan(Ratio.percentage(15)));
        assertEquals("15.51 %", ratio.toString());
        assertEquals("15.51237 %", ratio.toString(5));
        assertEquals(15.51236632, ratio.asPercentage(), DELTA);
        assertEquals(0.1551236632, ratio.asRatio(), DELTA);

        try
        {
            Ratio.percentage(101);
            fail();
        }
        catch (final Exception e)
        {
            // Expected
        }

        try
        {
            Ratio.percentage(-0.00001);
            fail();
        }
        catch (final Exception e)
        {
            // Expected
        }
    }
}

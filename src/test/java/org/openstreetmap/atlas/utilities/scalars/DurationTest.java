package org.openstreetmap.atlas.utilities.scalars;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class DurationTest
{
    @Test
    public void testConversion()
    {
        Duration duration = Duration.seconds(60);
        Assert.assertEquals(1.0, duration.asMinutes(), 2);

        duration = Duration.minutes(2);
        Assert.assertEquals(120000L, duration.asMilliseconds());

        duration = Duration.minutes(2);
        Assert.assertEquals(120000L, duration.asMilliseconds());
    }

    @Test
    public void testEquals()
    {
        Assert.assertTrue(Duration.seconds(3600).equals(Duration.hours(1)));
        Assert.assertFalse(Duration.seconds(1.001).equals(Duration.seconds(1.002)));
    }

    @Test
    public void testLowestAndHighest()
    {
        final Duration lowest = Duration.seconds(5);
        final Duration highest = Duration.seconds(10);
        Assert.assertEquals(lowest, lowest.lowest(highest));
        Assert.assertEquals(lowest, highest.lowest(lowest));
        Assert.assertEquals(highest, highest.lowest(null));
        Assert.assertEquals(highest, lowest.highest(highest));
        Assert.assertEquals(highest, highest.highest(lowest));
        Assert.assertEquals(lowest, lowest.highest(null));
    }
}

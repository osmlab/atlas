package org.openstreetmap.atlas.utilities.scalars;

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testSort()
    {
        final Duration duration1 = Duration.hours(2.9999999);
        final Duration duration2 = Duration.hours(3);
        final Duration duration3 = Duration.ONE_DAY;

        final List<Duration> durationSorted = Arrays.asList(duration1, duration2, duration3);
        final List<Duration> durationUnsorted = Arrays.asList(duration1, duration3, duration2);

        // get max min
        Assert.assertEquals(duration3, durationUnsorted.stream().max(Duration::compareTo).get());
        Assert.assertEquals(duration1, durationUnsorted.stream().min(Duration::compareTo).get());

        // sort and test
        durationUnsorted.sort(Duration::compareTo);
        Assert.assertEquals(durationSorted, durationUnsorted);
    }
}

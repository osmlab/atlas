package org.openstreetmap.atlas.utilities.scalars;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author tony
 */
public class SpeedTest
{
    @Test
    public void testCompare()
    {
        Assert.assertTrue(Speed.milesPerHour(10).isFasterThan(Speed.milesPerHour(9.999)));
        Assert.assertTrue(Speed.kilometersPerHour(100).isSlowerThan(Speed.kilometersPerHour(101)));
        Assert.assertTrue(Speed.kilometersPerHour(1.00008)
                .isFasterThanOrEqualTo(Speed.kilometersPerHour(1.00008)));
        Assert.assertTrue(Speed.kilometersPerHour(1.00009)
                .isFasterThanOrEqualTo(Speed.kilometersPerHour(1.00008)));
        Assert.assertTrue(Speed.kilometersPerHour(1.00008)
                .isSlowerThanOrEqualTo(Speed.kilometersPerHour(1.00008)));
        Assert.assertTrue(Speed.kilometersPerHour(1.00008)
                .isSlowerThanOrEqualTo(Speed.kilometersPerHour(1.00009)));
        Assert.assertFalse(Speed.kilometersPerHour(1.00009)
                .isSlowerThanOrEqualTo(Speed.kilometersPerHour(1.00008)));
        Assert.assertFalse(Speed.kilometersPerHour(1.00008)
                .isFasterThanOrEqualTo(Speed.kilometersPerHour(1.00009)));

        // This comparison will only track a few digits after decimal, depending on the
        // initialization methods you choose
        Assert.assertFalse(
                Speed.kilometersPerHour(1.000009).isSlowerThan(Speed.kilometersPerHour(1.000008)));
        Assert.assertFalse(Speed.kilometersPerHour(1.000009)
                .isSlowerThanOrEqualTo(Speed.kilometersPerHour(1.000008)));
    }

    @Test
    public void testConversion()
    {
        Assert.assertEquals(Speed.kilometersPerHour(10),
                Speed.distancePerDuration(Distance.kilometers(100), Duration.hours(10)));
        Assert.assertTrue(Speed.milesPerHour(1).asKilometersPerHour() - 1.6093440 < 0.0001);
    }

    @Test
    public void testDuration()
    {
        final Speed speed = Speed.metersPerSecond(3.0);
        final Distance distance = Distance.meters(6.0);
        Assert.assertEquals(Duration.seconds(2.0), speed.asDuration(distance));
    }

    @Test
    public void testEquals()
    {
        Assert.assertEquals(Speed.kilometersPerHour(9.999999), Speed.kilometersPerHour(9.999999));
        Assert.assertNotEquals(Speed.kilometersPerHour(9.99999), Speed.kilometersPerHour(9.99998));
        Assert.assertNotEquals(Speed.kilometersPerHour(9.9999999),
                Speed.kilometersPerHour(9.9999998));
    }

    @Test
    public void testOperations()
    {
        Assert.assertEquals(Speed.kilometersPerHour(100).difference(Speed.kilometersPerHour(50)),
                Speed.kilometersPerHour(50));
        Assert.assertEquals(Speed.kilometersPerHour(50).add(Speed.kilometersPerHour(50)),
                Speed.kilometersPerHour(100));
        Assert.assertEquals(Speed.kilometersPerHour(100).subtract(Speed.kilometersPerHour(50)),
                Speed.kilometersPerHour(50));
        Assert.assertEquals(Speed.kilometersPerHour(50).subtract(Speed.kilometersPerHour(100)),
                Speed.kilometersPerHour(0));
    }
}

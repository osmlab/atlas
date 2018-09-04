package org.openstreetmap.atlas.utilities.scalars;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;

/**
 * @author tony
 * @author matthieun
 */
public class DistanceTest
{
    @Test
    public void testAdd()
    {
        Assert.assertEquals(Distance.miles(10), Distance.miles(2).add(Distance.miles(8)));
        Assert.assertEquals(Distance.meters(100),
                Distance.millimeters(5000).add(Distance.meters(95)));
    }

    @Test
    public void testConversion()
    {
        final Distance tenMiles = Distance.miles(10);
        Assert.assertEquals(16093440.0, tenMiles.asMillimeters(), 5);
        Assert.assertEquals(52800.0, tenMiles.asFeet(), 0);
        Assert.assertEquals(16.09344, tenMiles.asKilometers(), 0);
        Assert.assertEquals(8.6897624, tenMiles.asNauticalMiles(), 1e-7);

        Assert.assertEquals(Distance.meters(tenMiles.asMeters()), tenMiles);
        Assert.assertEquals(Distance.miles(tenMiles.asMiles()), tenMiles);
        Assert.assertEquals(Distance.millimeters(tenMiles.asMillimeters()), tenMiles);
        Assert.assertEquals(Distance.nauticalMiles(tenMiles.asNauticalMiles()), tenMiles);
    }

    @Test
    public void testDistanceBetweenLocations()
    {
        final Location location1 = Location.forString("37.336900,-122.005414");
        final Location location2 = Location.forString("37.332758,-122.005409");

        final Distance approximation1 = location1.equirectangularDistanceTo(location2);
        final Distance approximation2 = location1.haversineDistanceTo(location2);

        Assert.assertTrue(Distance.FIFTEEN_HUNDRED_FEET.difference(approximation1)
                .isLessThan(Distance.meters(10)));
        Assert.assertTrue(Distance.FIFTEEN_HUNDRED_FEET.difference(approximation2)
                .isLessThan(Distance.meters(10)));

        // So the two methods have basically no difference, should choose equirectangular
        // approximation for most cases
        Assert.assertTrue(approximation1.difference(approximation2).isLessThan(Distance.meters(1)));
    }

    @Test
    public void testEquals()
    {
        Assert.assertTrue(Distance.meters(1000.001).equals(Distance.meters(1000.001)));
        Assert.assertFalse(Distance.meters(1000.001).equals(Distance.meters(1000.002)));
    }
}

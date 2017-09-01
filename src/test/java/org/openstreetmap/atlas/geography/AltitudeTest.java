package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * Tests {@link Altitude} functionality.
 *
 * @author mgostintsev
 */
public class AltitudeTest
{
    @Test
    public void testAltitudeEquals()
    {
        final Altitude positiveAltitude = Altitude.meters(40);
        final Altitude duplicatePositiveAltitude = Altitude.meters(40);
        final Altitude negativeAltitude = Altitude.meters(-40);

        Assert.assertNotEquals(positiveAltitude, negativeAltitude);
        Assert.assertEquals(positiveAltitude, duplicatePositiveAltitude);
    }

    @Test(expected = CoreException.class)
    public void testInvalidAltitude()
    {
        @SuppressWarnings("unused")
        final Altitude impossible = Altitude.meters(-7371000);
    }
}

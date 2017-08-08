package org.openstreetmap.atlas.utilities.scalars;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link Surface} scale function
 *
 * @author cstaylor
 */
public class SurfaceTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void failNegativeScale()
    {
        this.thrown.expect(IllegalArgumentException.class);
        Surface.UNIT_METER_SQUARED_ON_EARTH_SURFACE.scaleBy(-1);
    }

    @Test
    public void identityTest()
    {
        Assert.assertEquals(Surface.UNIT_METER_SQUARED_ON_EARTH_SURFACE,
                Surface.UNIT_METER_SQUARED_ON_EARTH_SURFACE.scaleBy(1));
    }

    @Test
    public void passZeroScale()
    {
        Assert.assertEquals(Surface.forDm7Squared(0),
                Surface.UNIT_METER_SQUARED_ON_EARTH_SURFACE.scaleBy(0));
    }
}

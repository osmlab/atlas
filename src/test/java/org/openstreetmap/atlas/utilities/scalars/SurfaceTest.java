package org.openstreetmap.atlas.utilities.scalars;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.utilities.testing.FreezeDryFunction;

/**
 * Unit test for {@link Surface} scale function
 *
 * @author cstaylor
 */
public class SurfaceTest
{
    private static final FreezeDryFunction<Surface> FREEZE_DRY = new FreezeDryFunction<>();

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
        Assert.assertEquals(FREEZE_DRY.apply(Surface.UNIT_METER_SQUARED_ON_EARTH_SURFACE),
                FREEZE_DRY.apply(Surface.UNIT_METER_SQUARED_ON_EARTH_SURFACE.scaleBy(1)));
    }

    @Test
    public void passZeroScale()
    {
        Assert.assertEquals(FREEZE_DRY.apply(Surface.forDm7Squared(0)),
                FREEZE_DRY.apply(Surface.UNIT_METER_SQUARED_ON_EARTH_SURFACE.scaleBy(0)));
    }
}

package org.openstreetmap.atlas.utilities.scalars;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test {@link Angle}s
 *
 * @author matthieun
 * @author mgostintsev
 */
public class AngleTest
{
    private static final Logger logger = LoggerFactory.getLogger(AngleTest.class);

    @Test
    public void testAddSubstract()
    {
        logger.info("Small: " + Angle.dm7(2320));
        final Angle ninety = Angle.degrees(90);
        logger.info("Ninety degrees: " + ninety);
        final Angle oneEighty = Angle.degrees(180);
        logger.info("One hundred and eighty degrees: " + oneEighty);
        final Angle twoSeventy = Angle.degrees(270);
        logger.info("Two hundred and seventy degrees: " + twoSeventy);
        Assert.assertEquals(ninety, twoSeventy.subtract(oneEighty));
        Assert.assertEquals(oneEighty, twoSeventy.subtract(ninety));
        Assert.assertEquals(twoSeventy, ninety.subtract(oneEighty));
        Assert.assertEquals(ninety, twoSeventy.add(oneEighty));
        Assert.assertEquals(twoSeventy, ninety.add(oneEighty));
    }

    @Test
    public void testConversion()
    {
        final Angle value = Angle.dm7(325_000_020);
        Assert.assertEquals(32.500002, value.asDegrees(), 0);
        Assert.assertEquals(0.5672320418047421, value.asRadians(), 1e-10);
    }

    @Test
    public void testDifference()
    {
        Assert.assertEquals(Angle.MAXIMUM, Angle.degrees(-180).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(-180).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(-180).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(-180).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(-180).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.degrees(0), Angle.degrees(-180).difference(Angle.degrees(180)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(-180).difference(Angle.degrees(200)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(-180).difference(Angle.degrees(250)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(-180).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(-180).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(-180).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.MAXIMUM, Angle.degrees(-180).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(-180).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.degrees(90), Angle.degrees(-90).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(-90).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(-90).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.MAXIMUM, Angle.degrees(-90).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(-90).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(-90).difference(Angle.degrees(180)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(-90).difference(Angle.degrees(200)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(-90).difference(Angle.degrees(250)));
        Assert.assertEquals(Angle.degrees(0), Angle.degrees(-90).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(-90).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(-90).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(-90).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(-90).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.degrees(0), Angle.degrees(0).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(0).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(0).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(0).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(0).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.MAXIMUM, Angle.degrees(0).difference(Angle.degrees(180)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(0).difference(Angle.degrees(200)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(0).difference(Angle.degrees(250)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(0).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(0).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(0).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.degrees(0), Angle.degrees(0).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(0).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.degrees(90), Angle.degrees(90).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(90).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(90).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.degrees(0), Angle.degrees(90).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(90).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(90).difference(Angle.degrees(180)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(90).difference(Angle.degrees(200)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(90).difference(Angle.degrees(250)));
        Assert.assertEquals(Angle.MAXIMUM, Angle.degrees(90).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(90).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(90).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(90).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(90).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.MAXIMUM, Angle.degrees(180).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(180).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(180).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(180).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(180).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.degrees(0), Angle.degrees(180).difference(Angle.degrees(180)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(180).difference(Angle.degrees(200)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(180).difference(Angle.degrees(250)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(180).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(180).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(180).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.MAXIMUM, Angle.degrees(180).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(180).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.degrees(90), Angle.degrees(270).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(270).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(160), Angle.degrees(270).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.MAXIMUM, Angle.degrees(270).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(270).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(270).difference(Angle.degrees(180)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(270).difference(Angle.degrees(200)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(270).difference(Angle.degrees(250)));
        Assert.assertEquals(Angle.degrees(0), Angle.degrees(270).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(20), Angle.degrees(270).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(70), Angle.degrees(270).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.degrees(90), Angle.degrees(270).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(110), Angle.degrees(270).difference(Angle.degrees(380)));
    }

    @Test
    public void testDistanceOnEarth()
    {
        final Angle value = Angle.dm7(Short.MAX_VALUE);
        Assert.assertEquals(364.352, value.onEarth().asMeters(), 1e-3);
    }

    @Test
    public void testPositiveAngle()
    {
        final Angle zero = Angle.degrees(0);
        Assert.assertEquals(zero, zero.asPositiveAngle());
        Assert.assertEquals(Angle.degrees(10), Angle.degrees(-10).asPositiveAngle());
        Assert.assertEquals(Angle.MAXIMUM, Angle.MINIMUM.asPositiveAngle());
        Assert.assertEquals(Angle.MAXIMUM, Angle.MAXIMUM.asPositiveAngle());
    }

    @Test
    public void testPositiveRadians()
    {
        final Angle value = Angle.MINIMUM;
        Assert.assertEquals(-Math.PI, value.asRadians(), 1e-9);
        Assert.assertEquals(Math.PI, value.asPositiveRadians(), 1e-9);
    }

    @Test
    public void testRollingValue()
    {
        final Angle value = Angle.dm7(3_600_000_050L);
        Assert.assertEquals(value, Angle.dm7(50));
    }
}

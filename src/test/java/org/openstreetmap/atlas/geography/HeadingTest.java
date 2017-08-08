package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * Test {@link Heading}s
 *
 * @author mkalender
 */
public class HeadingTest
{
    @Test
    public void testAngles()
    {
        Assert.assertEquals(Angle.degrees(-360), Heading.SOUTH);
        Assert.assertEquals(Angle.degrees(-270), Heading.WEST);
        Assert.assertEquals(Angle.degrees(-180), Heading.NORTH);
        Assert.assertEquals(Angle.degrees(-90), Heading.EAST);
        Assert.assertEquals(Angle.degrees(0), Heading.SOUTH);
        Assert.assertEquals(Angle.degrees(90), Heading.WEST);
        Assert.assertEquals(Angle.degrees(180), Heading.NORTH);
        Assert.assertEquals(Angle.degrees(270), Heading.EAST);
        Assert.assertEquals(Angle.degrees(360), Heading.SOUTH);
        Assert.assertEquals(Angle.degrees(450), Heading.WEST);
    }

    @Test
    public void testDifference()
    {
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(-180).difference(Heading.degrees(0)));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(20)),
                Angle.degrees(160));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(70)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(90)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(160)),
                Angle.degrees(20));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(180)),
                Angle.degrees(0));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(200)),
                Angle.degrees(20));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(250)),
                Angle.degrees(70));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(270)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(290)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(340)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(-180).difference(Heading.degrees(360)));
        Assert.assertEquals(Heading.degrees(-180).difference(Heading.degrees(380)),
                Angle.degrees(160));

        Assert.assertEquals(Angle.degrees(90), Heading.degrees(-90).difference(Heading.degrees(0)));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(20)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(70)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(-90).difference(Heading.degrees(90)));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(160)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(180)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(200)),
                Angle.degrees(70));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(250)),
                Angle.degrees(20));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(270)),
                Angle.degrees(0));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(290)),
                Angle.degrees(20));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(340)),
                Angle.degrees(70));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(360)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(-90).difference(Heading.degrees(380)),
                Angle.degrees(110));

        Assert.assertEquals(Angle.degrees(0), Heading.degrees(0).difference(Heading.degrees(0)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(0).difference(Heading.degrees(20)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(0).difference(Heading.degrees(70)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(0).difference(Heading.degrees(90)));
        Assert.assertEquals(Heading.degrees(0).difference(Heading.degrees(160)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(0).difference(Heading.degrees(180)));
        Assert.assertEquals(Heading.degrees(0).difference(Heading.degrees(200)),
                Angle.degrees(160));
        Assert.assertEquals(Heading.degrees(0).difference(Heading.degrees(250)),
                Angle.degrees(110));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(0).difference(Heading.degrees(270)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(0).difference(Heading.degrees(290)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(0).difference(Heading.degrees(340)));
        Assert.assertEquals(Angle.degrees(0), Heading.degrees(0).difference(Heading.degrees(360)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(0).difference(Heading.degrees(380)));

        Assert.assertEquals(Angle.degrees(90), Heading.degrees(90).difference(Heading.degrees(0)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(90).difference(Heading.degrees(20)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(90).difference(Heading.degrees(70)));
        Assert.assertEquals(Angle.degrees(0), Heading.degrees(90).difference(Heading.degrees(90)));
        Assert.assertEquals(Heading.degrees(90).difference(Heading.degrees(160)),
                Angle.degrees(70));
        Assert.assertEquals(Heading.degrees(90).difference(Heading.degrees(180)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(90).difference(Heading.degrees(200)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(90).difference(Heading.degrees(250)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(90).difference(Heading.degrees(270)));
        Assert.assertEquals(Heading.degrees(90).difference(Heading.degrees(290)),
                Angle.degrees(160));
        Assert.assertEquals(Heading.degrees(90).difference(Heading.degrees(340)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(90).difference(Heading.degrees(360)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(90).difference(Heading.degrees(380)),
                Angle.degrees(70));

        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(180).difference(Heading.degrees(0)));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(20)),
                Angle.degrees(160));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(70)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(90)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(160)),
                Angle.degrees(20));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(180)),
                Angle.degrees(0));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(200)),
                Angle.degrees(20));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(250)),
                Angle.degrees(70));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(270)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(290)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(340)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(180).difference(Heading.degrees(360)));
        Assert.assertEquals(Heading.degrees(180).difference(Heading.degrees(380)),
                Angle.degrees(160));

        Assert.assertEquals(Angle.degrees(90), Heading.degrees(270).difference(Heading.degrees(0)));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(20)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(70)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(270).difference(Heading.degrees(90)));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(160)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(180)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(200)),
                Angle.degrees(70));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(250)),
                Angle.degrees(20));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(270)),
                Angle.degrees(0));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(290)),
                Angle.degrees(20));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(340)),
                Angle.degrees(70));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(360)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(270).difference(Heading.degrees(380)),
                Angle.degrees(110));
    }

    @Test
    public void testHeadingAngleDifference()
    {
        Assert.assertEquals(Angle.NONE, Heading.degrees(-180).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(-180).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(-180).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(-180).difference(Angle.degrees(90)));
        Assert.assertEquals(Heading.degrees(-180).difference(Angle.degrees(160)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(-180).difference(Angle.degrees(180)));
        Assert.assertEquals(Heading.degrees(-180).difference(Angle.degrees(200)),
                Angle.degrees(160));
        Assert.assertEquals(Heading.degrees(-180).difference(Angle.degrees(250)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(-180).difference(Angle.degrees(270)),
                Angle.degrees(90));
        Assert.assertEquals(Heading.degrees(-180).difference(Angle.degrees(290)),
                Angle.degrees(70));
        Assert.assertEquals(Heading.degrees(-180).difference(Angle.degrees(340)),
                Angle.degrees(20));
        Assert.assertEquals(Angle.NONE, Heading.degrees(-180).difference(Angle.degrees(360)));
        Assert.assertEquals(Heading.degrees(-180).difference(Angle.degrees(380)),
                Angle.degrees(20));

        Assert.assertEquals(Angle.degrees(90), Heading.degrees(-90).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(-90).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(-90).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.NONE, Heading.degrees(-90).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(-90).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(-90).difference(Angle.degrees(180)));
        Assert.assertEquals(Heading.degrees(-90).difference(Angle.degrees(200)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(-90).difference(Angle.degrees(250)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(-90).difference(Angle.degrees(270)));
        Assert.assertEquals(Heading.degrees(-90).difference(Angle.degrees(290)),
                Angle.degrees(160));
        Assert.assertEquals(Heading.degrees(-90).difference(Angle.degrees(340)),
                Angle.degrees(110));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(-90).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(-90).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(0).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(160), Heading.degrees(0).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(110), Heading.degrees(0).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(0).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(0).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.NONE, Heading.degrees(0).difference(Angle.degrees(180)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(0).difference(Angle.degrees(200)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(0).difference(Angle.degrees(250)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(0).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(110), Heading.degrees(0).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(160), Heading.degrees(0).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(0).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(160), Heading.degrees(0).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.degrees(90), Heading.degrees(90).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(110), Heading.degrees(90).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(160), Heading.degrees(90).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(90).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(110), Heading.degrees(90).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(90).difference(Angle.degrees(180)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(90).difference(Angle.degrees(200)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(90).difference(Angle.degrees(250)));
        Assert.assertEquals(Angle.NONE, Heading.degrees(90).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(90).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(90).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(90).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(110), Heading.degrees(90).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.NONE, Heading.degrees(180).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(180).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(180).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(180).difference(Angle.degrees(90)));
        Assert.assertEquals(Heading.degrees(180).difference(Angle.degrees(160)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(180).difference(Angle.degrees(180)));
        Assert.assertEquals(Heading.degrees(180).difference(Angle.degrees(200)),
                Angle.degrees(160));
        Assert.assertEquals(Heading.degrees(180).difference(Angle.degrees(250)),
                Angle.degrees(110));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(180).difference(Angle.degrees(270)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(180).difference(Angle.degrees(290)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(180).difference(Angle.degrees(340)));
        Assert.assertEquals(Angle.NONE, Heading.degrees(180).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(180).difference(Angle.degrees(380)));

        Assert.assertEquals(Angle.degrees(90), Heading.degrees(270).difference(Angle.degrees(0)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(270).difference(Angle.degrees(20)));
        Assert.assertEquals(Angle.degrees(20), Heading.degrees(270).difference(Angle.degrees(70)));
        Assert.assertEquals(Angle.NONE, Heading.degrees(270).difference(Angle.degrees(90)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(270).difference(Angle.degrees(160)));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(270).difference(Angle.degrees(180)));
        Assert.assertEquals(Heading.degrees(270).difference(Angle.degrees(200)),
                Angle.degrees(110));
        Assert.assertEquals(Heading.degrees(270).difference(Angle.degrees(250)),
                Angle.degrees(160));
        Assert.assertEquals(Angle.MAXIMUM, Heading.degrees(270).difference(Angle.degrees(270)));
        Assert.assertEquals(Heading.degrees(270).difference(Angle.degrees(290)),
                Angle.degrees(160));
        Assert.assertEquals(Heading.degrees(270).difference(Angle.degrees(340)),
                Angle.degrees(110));
        Assert.assertEquals(Angle.degrees(90), Heading.degrees(270).difference(Angle.degrees(360)));
        Assert.assertEquals(Angle.degrees(70), Heading.degrees(270).difference(Angle.degrees(380)));
    }
}

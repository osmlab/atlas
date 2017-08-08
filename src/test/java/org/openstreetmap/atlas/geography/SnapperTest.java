package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class SnapperTest
{
    @Test
    public void testPolyLine()
    {
        final PolyLine shape = new PolyLine(Location.TEST_6, Location.TEST_1,
                Location.EIFFEL_TOWER);
        final Location origin = Location.TEST_2;
        Assert.assertEquals(Location.forString("37.3268107,-122.030562"), origin.snapTo(shape));
        Assert.assertEquals(Location.TEST_6, shape.snapFrom(Location.TEST_3));
        Assert.assertEquals(Location.EIFFEL_TOWER, shape.snapFrom(Location.COLOSSEUM));
    }

    @Test
    public void testSegment()
    {
        final Segment shape = new Segment(Location.TEST_6, Location.TEST_1);
        final Location origin = Location.TEST_2;
        Assert.assertEquals(Location.forString("37.3268107,-122.030562"), origin.snapTo(shape));
        Assert.assertEquals(Location.TEST_6, shape.snapFrom(Location.TEST_3));
        Assert.assertEquals(Location.TEST_1, shape.snapFrom(Location.EIFFEL_TOWER));
    }
}

package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class SnapperTest
{
    @Test
    public void testMultiPolygon()
    {
        final MultiPolygon shape = MultiPolygon.TEST_MULTI_POLYGON;
        final Location origin1 = Location.forString("37.328709, -122.032873");
        final Location origin2 = Location.forString("37.324014, -122.046642");
        Assert.assertEquals(Location.forString("37.3283544,-122.0322605"), origin1.snapTo(shape));
        Assert.assertEquals(Location.forString("37.3249067,-122.0459561"), origin2.snapTo(shape));
    }

    @Test
    public void testPolygon()
    {
        final Polygon shape = new Polygon(Location.TEST_6, Location.TEST_1, Location.EIFFEL_TOWER);
        final Location origin = Location.forString("37.325315, -122.008007");
        Assert.assertEquals(Location.forString("37.3278247,-122.0082398"), origin.snapTo(shape));
    }

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

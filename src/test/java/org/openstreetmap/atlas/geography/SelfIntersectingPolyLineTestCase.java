package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for the self intersecting line API.
 *
 * @author cstaylor
 */
public class SelfIntersectingPolyLineTestCase
{
    private static final Location ONE = Location.forString("40.0000001, -80.0000003");
    private static final Location TWO = Location.forString("40.0000001, -80.0000001");
    private static final Location THREE = Location.forString("40.0000003, -80.0000001");
    private static final Location FOUR = Location.forString("40.0000003, -80.0000003");

    @Test
    public void noSelfIntersection()
    {
        final PolyLine line = new PolyLine(ONE, TWO, THREE, FOUR);
        Assert.assertFalse(line.selfIntersects());
    }

    @Test
    public void selfIntersects()
    {
        final PolyLine line = new PolyLine(ONE, TWO, THREE, FOUR, TWO);
        Assert.assertTrue(line.selfIntersects());
    }
}

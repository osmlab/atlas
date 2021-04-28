package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author matthieun
 */
public class SegmentTest
{

    private static Segment SEGMENT_SAME_END_1 = PolyLine
            .wkt("LINESTRING (112.9699474 -84.7999528, 112.9699948 -84.7999669)").segments().get(0);
    private static Segment SEGMENT_SAME_END_2 = PolyLine
            .wkt("LINESTRING (112.9650809 -84.7999622, 112.9699948 -84.7999669)").segments().get(0);

    @Test
    public void testAntimeridianNoIntersection()
    {
        final Segment antimeridianWest = new Segment(
                new Location(Latitude.degrees(40), Longitude.ANTIMERIDIAN_WEST),
                new Location(Latitude.degrees(41), Longitude.ANTIMERIDIAN_WEST));

        final Segment antimeridianEast = new Segment(
                new Location(Latitude.degrees(40), Longitude.ANTIMERIDIAN_EAST),
                new Location(Latitude.degrees(41), Longitude.ANTIMERIDIAN_EAST));
        Assert.assertNull(antimeridianEast.intersection(antimeridianWest));
        Assert.assertTrue(antimeridianEast.length().equals(antimeridianWest.length()));
        Assert.assertTrue(antimeridianEast.length().isLessThan(Distance.miles(100)));
    }

    @Test
    public void testIntersection()
    {
        Assert.assertEquals(null, new Segment(Location.TEST_1, Location.TEST_2)
                .intersection(new Segment(Location.TEST_4, Location.TEST_3)));
        Assert.assertEquals(false, new Segment(Location.TEST_1, Location.TEST_2)
                .intersects(new Segment(Location.TEST_4, Location.TEST_3)));

        Assert.assertEquals(
                new Location(Latitude.degrees(37.3273389), Longitude.degrees(-122.0287109)),
                new Segment(Location.TEST_1, Location.TEST_3)
                        .intersection(new Segment(Location.TEST_4, Location.TEST_2)));
        Assert.assertEquals(true, new Segment(Location.TEST_1, Location.TEST_3)
                .intersects(new Segment(Location.TEST_4, Location.TEST_2)));
    }

    @Test
    public void testIntersectionOverflow()
    {
        final Segment maxXandYMovement = new Segment(
                new Location(Latitude.MINIMUM, Longitude.MINIMUM),
                new Location(Latitude.MAXIMUM, Longitude.MAXIMUM));
        final Segment maxXandNegativeYMovement = new Segment(
                new Location(Latitude.MAXIMUM, Longitude.MINIMUM),
                new Location(Latitude.MINIMUM, Longitude.MAXIMUM));

        Assert.assertTrue("Overflow issue", maxXandYMovement.intersects(maxXandNegativeYMovement));
        Assert.assertTrue("Overflow issue", maxXandNegativeYMovement.intersects(maxXandYMovement));

    }

    @Test
    public void testIntersectionWithSameEnd()
    {
        Assert.assertTrue("Same start is broken.",
                SEGMENT_SAME_END_1.reversed().intersects(SEGMENT_SAME_END_2.reversed()));
        Assert.assertTrue("Same start is broken.",
                SEGMENT_SAME_END_2.reversed().intersects(SEGMENT_SAME_END_1.reversed()));
        Assert.assertTrue("Same End is broken", SEGMENT_SAME_END_1.intersects(SEGMENT_SAME_END_2));
        Assert.assertTrue("Same End is broken", SEGMENT_SAME_END_2.intersects(SEGMENT_SAME_END_1));
    }
}

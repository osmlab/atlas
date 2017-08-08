package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class SegmentTest
{
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
}

package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author mgostintsev
 */
public class AreaTest
{
    private static final Logger logger = LoggerFactory.getLogger(AreaTest.class);

    @Rule
    public final AreaTestRule rule = new AreaTestRule();

    @Test
    public void testAreaGeometry()
    {
        final Atlas atlas = this.rule.getAtlas();

        final Iterable<Location> edge1RawGeometry = atlas.area(1).getRawGeometry();
        logger.info("Edge 1 raw geometry {}", edge1RawGeometry);

        final Iterable<Location> edge1ClosedGeometry = atlas.area(1).getClosedGeometry();
        logger.info("Edge 1 closed geometry {}", Lists.newArrayList(edge1ClosedGeometry));

        final Iterable<Location> edge2RawGeometry = atlas.area(2).getRawGeometry();
        logger.info("Edge 2 raw geometry {}", edge2RawGeometry);

        final Iterable<Location> edge2ClosedGeometry = atlas.area(2).getClosedGeometry();
        logger.info("Edge 2 closed geometry {}", Lists.newArrayList(edge2ClosedGeometry));

        // Assert single node difference between closed and raw geometry
        Assert.assertEquals(Iterables.size(edge1RawGeometry) + 1,
                Iterables.size(edge1ClosedGeometry));
        Assert.assertEquals(Iterables.size(edge2RawGeometry) + 1,
                Iterables.size(edge2ClosedGeometry));
        // Assert raw geometry start and closed geometry end are equal
        Assert.assertEquals(Iterables.getLast(edge1ClosedGeometry),
                Iterables.get(edge1RawGeometry, 0));
        Assert.assertEquals(Iterables.getLast(edge2ClosedGeometry),
                Iterables.get(edge2RawGeometry, 0));
        // Assert start and end of closed geometry are equal
        Assert.assertEquals(Iterables.getLast(edge1ClosedGeometry),
                Iterables.get(edge1ClosedGeometry, 0));
        Assert.assertEquals(Iterables.getLast(edge2ClosedGeometry),
                Iterables.get(edge2ClosedGeometry, 0));
    }
}

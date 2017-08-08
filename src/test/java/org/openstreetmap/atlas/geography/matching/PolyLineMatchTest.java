package org.openstreetmap.atlas.geography.matching;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class PolyLineMatchTest
{
    private static final Logger logger = LoggerFactory.getLogger(PolyLineMatchTest.class);

    @Test
    public void testMatch()
    {
        final PolyLine source = new PolyLine(Location.CROSSING_85_280, Location.TEST_2,
                Location.TEST_1);
        final PolyLine candidate1 = new PolyLine(Location.CROSSING_85_280, Location.TEST_2,
                Location.TEST_5);
        final PolyLine candidate2 = new PolyLine(Location.CROSSING_85_17, Location.TEST_2,
                Location.TEST_1);
        final List<PolyLine> candidates = Iterables
                .asList(Iterables.iterable(candidate1, candidate2));
        final PolyLineRoute route = source.costDistanceToOneWay(candidates)
                .match(Distance.ZERO, Duration.ONE_MINUTE)
                .orElseThrow(() -> new CoreException("Did not find a route!"));
        logger.info("{}", route);
        Assert.assertEquals(3, route.asPolyLine().size());
    }
}

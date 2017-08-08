package org.openstreetmap.atlas.geography.atlas.routing;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasRoutingIntegrationTest extends AtlasIntegrationTest
{
    /**
     * @author matthieun
     */
    private static final class ExpectedRoute
    {
        private final Location start;
        private final Location end;
        private final Distance expectedMaximumLength;
        private final int expectedMaximumSize;

        ExpectedRoute(final Location start, final Location end,
                final double expectedMaximumLengthInMeters, final int expectedMaximumSize)
        {
            this.start = start;
            this.end = end;
            this.expectedMaximumLength = Distance.meters(expectedMaximumLengthInMeters);
            this.expectedMaximumSize = expectedMaximumSize;
        }

        public Location getEnd()
        {
            return this.end;
        }

        public Distance getExpectedMaximumLength()
        {
            return this.expectedMaximumLength;
        }

        public int getExpectedMaximumSize()
        {
            return this.expectedMaximumSize;
        }

        public Location getStart()
        {
            return this.start;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AtlasRoutingIntegrationTest.class);

    private static final Location ONE = Location.forString("20.245996,-74.1502787");
    private static final Location TWO = Location.forString("20.0774286,-74.4971688");
    private static final Location THREE = Location.forString("20.6079142,-74.7530228");
    private static final Location FOUR = Location.forString("20.6649509,-74.9532838");
    private static final Location FIVE = Location.forString("20.58069,-75.24285");
    private static final Location SIX = Location.forString("20.13990,-75.20937");

    private static final List<ExpectedRoute> EXPECTED_ROUTES = new ArrayList<>();

    static
    {
        EXPECTED_ROUTES.add(new ExpectedRoute(ONE, TWO, 70_000, 50));
        EXPECTED_ROUTES.add(new ExpectedRoute(TWO, THREE, 160_000, 150));
        EXPECTED_ROUTES.add(new ExpectedRoute(THREE, FOUR, 30_000, 80));
        EXPECTED_ROUTES.add(new ExpectedRoute(FOUR, FIVE, 60_000, 90));
        EXPECTED_ROUTES.add(new ExpectedRoute(FIVE, SIX, 100_000, 110));
    }

    private Atlas cuba;
    private Router router;

    @After
    public void destroy()
    {
        this.cuba = null;
        this.router = null;
    }

    @Before
    public void initialize()
    {
        this.cuba = loadCuba();
        this.router = AStarRouter.fastComputationAndSubOptimalRoute(this.cuba,
                Distance.meters(100));
    }

    @Test
    public void testRouting()
    {
        for (final ExpectedRoute expectedRoute : EXPECTED_ROUTES)
        {
            final Route route = route(expectedRoute.getStart(), expectedRoute.getEnd());
            Assert.assertTrue(expectedRoute.getExpectedMaximumSize() >= route.size());
            Assert.assertTrue(expectedRoute.getExpectedMaximumLength()
                    .isGreaterThanOrEqualTo(route.length()));
        }
    }

    private Route route(final Location start, final Location end)
    {
        final Time beginning = Time.now();
        final Route route = this.router.route(start, end);
        if (route == null)
        {
            throw new CoreException("Could not find route between {} and {}.", start, end);
        }
        logger.info("Computed route between {} and {}, with {} edges, {} long, in {}", start, end,
                route.size(), route.length(), beginning.elapsedSince());
        return route;
    }
}

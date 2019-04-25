package org.openstreetmap.atlas.geography.atlas.routing;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author matthieun
 */
public class AStarRouterTest
{
    @Rule
    public final AStarRouterTestRule rule = new AStarRouterTestRule();
    private final Distance threshold = Distance.meters(40);

    private Atlas atlas1;
    private Atlas atlas2;

    private Atlas multiAtlas;

    @Before
    public void setup()
    {
        this.atlas1 = this.rule.getAtlas1();
        this.atlas2 = this.rule.getAtlas2();
        this.multiAtlas = new MultiAtlas(this.atlas1, this.atlas2);
    }

    @Test
    public void testAlgorithm()
    {
        final Location start = Location.TEST_6.shiftAlongGreatCircle(Heading.NORTH,
                Distance.ONE_METER);
        final Location end = Location.TEST_2.shiftAlongGreatCircle(Heading.EAST,
                Distance.ONE_METER);
        final Route dijkstraRoute = AStarRouter.dijkstra(this.multiAtlas, this.threshold)
                .route(start, end);
        Assert.assertEquals(Route.forEdges(this.multiAtlas.edge(9), this.multiAtlas.edge(-9),
                this.multiAtlas.edge(5), this.multiAtlas.edge(6)), dijkstraRoute);
        final Route balancedRoute = AStarRouter.balanced(this.multiAtlas, this.threshold)
                .route(start, end);
        Assert.assertEquals(Route.forEdges(this.multiAtlas.edge(9), this.multiAtlas.edge(-9),
                this.multiAtlas.edge(5), this.multiAtlas.edge(6)), balancedRoute);
    }

    @Test
    public void testEdgeCases()
    {
        final AtlasBuilder builder = new PackedAtlasBuilder();
        final Map<String, String> tags = new HashMap<>();
        final Location location1 = Location.TEST_6;
        final Location location2 = Location.TEST_2;
        final Location location3 = Location.TEST_1;
        builder.addNode(1, location1, tags);
        builder.addNode(2, location2, tags);
        builder.addNode(3, location3, tags);
        builder.addEdge(1, new Segment(location1, location2), tags);
        builder.addEdge(2, new Segment(location2, location3), tags);
        final Atlas routeAtlas = builder.get();

        final AStarRouter router = AStarRouter.dijkstra(routeAtlas, this.threshold);
        // Same edge
        Assert.assertEquals(Route.forEdge(routeAtlas.edge(1)),
                router.route(routeAtlas.edge(1), routeAtlas.edge(1)));
        Assert.assertEquals(Route.forEdges(routeAtlas.edge(1), routeAtlas.edge(2)),
                router.route(routeAtlas.node(1), routeAtlas.node(3)));
        Assert.assertEquals(Route.forEdges(routeAtlas.edge(1), routeAtlas.edge(2)),
                router.route(routeAtlas.edge(1), routeAtlas.edge(2)));
        try
        {
            router.route(routeAtlas.edge(1), null);
            Assert.fail("Did not throw exception");
        }
        catch (final CoreException e)
        {
            // Expected
        }
        catch (final Exception e)
        {
            Assert.fail("Did not throw proper exception");
        }

        final Location start = location1.shiftAlongGreatCircle(Heading.EAST, Distance.ONE_METER);
        final Location end = location3.shiftAlongGreatCircle(Heading.WEST, Distance.ONE_METER);
        Assert.assertEquals(Route.forEdges(routeAtlas.edge(1), routeAtlas.edge(2)),
                router.route(start, end));
    }
}

package org.openstreetmap.atlas.geography.atlas.routing;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlasTest;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author matthieun
 */
public class AStarRouterTest
{
    private final Distance threshold = Distance.meters(40);

    @Test
    public void testAlgorithm()
    {
        final Atlas atlas = new MultiAtlasTest().getAtlas();
        final Location start = Location.TEST_6.shiftAlongGreatCircle(Heading.NORTH,
                Distance.ONE_METER);
        final Location end = Location.TEST_2.shiftAlongGreatCircle(Heading.EAST,
                Distance.ONE_METER);
        final Route dijkstraRoute = AStarRouter.dijkstra(atlas, this.threshold).route(start, end);
        System.out.println(dijkstraRoute);
        Assert.assertEquals(
                Route.forEdges(atlas.edge(9), atlas.edge(-9), atlas.edge(5), atlas.edge(6)),
                dijkstraRoute);
        final Route balancedRoute = AStarRouter.balanced(atlas, this.threshold).route(start, end);
        System.out.println(balancedRoute);
        Assert.assertEquals(
                Route.forEdges(atlas.edge(9), atlas.edge(-9), atlas.edge(5), atlas.edge(6)),
                balancedRoute);
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
        final Atlas atlas = builder.get();

        final AStarRouter router = AStarRouter.dijkstra(atlas, this.threshold);
        // Same edge
        Assert.assertEquals(Route.forEdge(atlas.edge(1)),
                router.route(atlas.edge(1), atlas.edge(1)));
        Assert.assertEquals(Route.forEdges(atlas.edge(1), atlas.edge(2)),
                router.route(atlas.node(1), atlas.node(3)));
        Assert.assertEquals(Route.forEdges(atlas.edge(1), atlas.edge(2)),
                router.route(atlas.edge(1), atlas.edge(2)));
        try
        {
            router.route(atlas.edge(1), null);
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
        Assert.assertEquals(Route.forEdges(atlas.edge(1), atlas.edge(2)), router.route(start, end));
    }
}

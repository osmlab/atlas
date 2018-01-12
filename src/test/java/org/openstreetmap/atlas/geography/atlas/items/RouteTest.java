package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasTest;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class RouteTest
{
    private Atlas atlas;
    private Route route;

    @Rule
    public final RouteTestRule rule = new RouteTestRule();

    @Before
    public void init()
    {
        this.atlas = new PackedAtlasTest().getAtlas();
        final Edge edge1 = this.atlas.edge(98);
        final Edge edge2 = this.atlas.edge(987);

        this.route = Route.forEdge(edge1);
        this.route = this.route.append(edge2);
    }

    @Test(expected = CoreException.class)
    public void testGetIndex()
    {
        Assert.assertEquals(98, this.route.get(0).getIdentifier());
        Assert.assertEquals(987, this.route.get(1).getIdentifier());

        this.route.get(-7);
        Assert.fail("Should complain about wrong index");

        this.route.get(7);
        Assert.fail("Should complain about wrong index");
    }

    /**
     * Before the addition of the start and end {@link Node}s to the {@link Route #hashCode()}, the
     * {@link Route}s in this test would generate hash code collisions. This test verifies the
     * collisions no longer occur.
     */
    @Test
    public void testHashCode()
    {
        final Atlas atlas = this.rule.getRouteHashCodeAtlas();
        final Edge edge1 = atlas.edge(-206786592000008L);
        final Edge edge2 = atlas.edge(206786592000008L);
        final Edge edge3 = atlas.edge(206786592000007L);
        final Edge edge4 = atlas.edge(-206786592000007L);

        final Route route1 = Route.forEdges(edge1, edge2);
        final Route route2 = Route.forEdges(edge3, edge2);

        Assert.assertTrue("Route 1 and Route 2 no longer collide",
                route1.hashCode() != route2.hashCode());

        final Route route3 = Route.forEdges(edge1, edge4);
        final Route route4 = Route.forEdges(edge3, edge4);

        Assert.assertTrue("Route 3 and Route 4 no longer collide",
                route3.hashCode() != route4.hashCode());
    }

    @Test
    public void testOverlappingRoutes()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Route startOnlyRoute = Route.forEdge(atlas.edge(159019301));
        final Route middleOnlyRoute = Route.forEdge(atlas.edge(128620751));
        final Route endOnlyRoute = Route.forEdge(atlas.edge(128620796));
        final Route shorterRouteAtBeginning = Route.forEdges(atlas.edge(159019301),
                atlas.edge(128620751));
        final Route shorterRouteAtEnd = Route.forEdges(atlas.edge(128620751),
                atlas.edge(128620796));
        final Route longerRoute = Route.forEdges(atlas.edge(159019301), atlas.edge(128620751),
                atlas.edge(128620796));
        final Route divergingRoute = Route.forEdges(atlas.edge(128620751), atlas.edge(138620888));

        Assert.assertEquals(
                "The route with only the start edge should overlap at its 0th and only index", 0,
                startOnlyRoute.overlapIndex(longerRoute));
        Assert.assertEquals(
                "The route with only the middle edge should overlap at its 0th and only index", 0,
                middleOnlyRoute.overlapIndex(longerRoute));
        Assert.assertEquals(
                "The route with only the end edge should overlap at its 0th and only index", 0,
                endOnlyRoute.overlapIndex(longerRoute));
        Assert.assertEquals("The shorter route should overlap at its last (1st) index", 1,
                shorterRouteAtBeginning.overlapIndex(longerRoute));
        Assert.assertEquals("The same should hold true in reverse", 1,
                longerRoute.overlapIndex(shorterRouteAtBeginning));
        Assert.assertEquals("The shorter route should overlap at its last (1st) index", 1,
                shorterRouteAtEnd.overlapIndex(longerRoute));
        Assert.assertEquals("The entire route should overlap at its middle (1st) index", 1,
                longerRoute.overlapIndex(shorterRouteAtBeginning));
        Assert.assertEquals("The entire route should overlap at its last (2nd) index", 2,
                longerRoute.overlapIndex(shorterRouteAtEnd));
        Assert.assertEquals("The entire route should overlap at its first (0th) index", 0,
                longerRoute.overlapIndex(startOnlyRoute));
        Assert.assertEquals("The entire route should overlap at its middle (1st) index", 1,
                longerRoute.overlapIndex(middleOnlyRoute));
        Assert.assertEquals("The entire route should overlap at its last (2nd) index", 2,
                longerRoute.overlapIndex(endOnlyRoute));
        Assert.assertEquals("No overlap should happen", -1,
                startOnlyRoute.overlapIndex(shorterRouteAtEnd));
        Assert.assertEquals("No overlap should happen", -1,
                shorterRouteAtEnd.overlapIndex(startOnlyRoute));
        Assert.assertEquals("No overlap should happen", -1,
                longerRoute.overlapIndex(divergingRoute));
    }

    @Test(expected = CoreException.class)
    public void testRoute()
    {
        this.route.append(this.atlas.edge(-9));
        Assert.fail("Should not build non connected route");
    }

    @Test
    public void testStartsWith()
    {
        final Atlas atlas = this.rule.getAtlas();

        final Route route = Route.forEdges(atlas.edge(159019301), atlas.edge(128620751),
                atlas.edge(128620796));
        final Route duplicateRoute = Route.forEdges(atlas.edge(159019301), atlas.edge(128620751),
                atlas.edge(128620796));
        final Route multiRoute = Route.forEdges(atlas.edge(159019301), atlas.edge(128620751));
        final Route singleRoute = Route.forEdges(atlas.edge(159019301));
        final Route incorrectMultiRoute = Route.forEdges(atlas.edge(159019301),
                atlas.edge(128620751), atlas.edge(138620888));
        final Route incorrectSingleRoute = Route.forEdge(atlas.edge(128620751));

        Assert.assertTrue("Verify startsWith returns true when passing in the same route",
                route.startsWith(duplicateRoute));

        Assert.assertTrue("Verify startsWith returns true for a multiRoute",
                route.startsWith(multiRoute));
        Assert.assertTrue("Verify startsWith returns true for a singleRoute",
                route.startsWith(singleRoute));

        Assert.assertFalse("Verify a startsWith negative case for a multiRoute",
                route.startsWith(incorrectMultiRoute));
        Assert.assertFalse("Verify a startsWith negative case for a singleRoute",
                route.startsWith(incorrectSingleRoute));

        Assert.assertFalse(
                "Verify startsWith returns false when other route is longer than current route",
                singleRoute.startsWith(multiRoute));
    }

    @Test
    public void testSubRoutes()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Route shorterRoute = Route.forEdges(atlas.edge(159019301), atlas.edge(128620751));
        final Route longerRoute = Route.forEdges(atlas.edge(159019301), atlas.edge(128620751),
                atlas.edge(128620796));
        final Route partialLongerRoute = Route.forEdges(atlas.edge(128620751),
                atlas.edge(128620796));
        final Route middleEdgeRoute = Route.forEdge(atlas.edge(128620751));

        // SubRouteIndex and isSubRoute tests
        Assert.assertEquals("Longer route cannot be a subroute of the a shorter one", -1,
                shorterRoute.subRouteIndex(longerRoute));
        Assert.assertFalse(shorterRoute.isSubRoute(longerRoute));

        Assert.assertEquals(
                "Shorter route is a subroute of the longer one, with the last overlap at index 1",
                1, longerRoute.subRouteIndex(shorterRoute));
        Assert.assertTrue(longerRoute.isSubRoute(shorterRoute));

        // subRoute tests
        Assert.assertEquals("Subroute the last edge from a route", shorterRoute,
                longerRoute.subRoute(0, shorterRoute.size()));

        Assert.assertEquals("Subroute the first edge from a route", partialLongerRoute,
                longerRoute.subRoute(1, longerRoute.size()));

        Assert.assertEquals("Subroute a middle edge from a route", middleEdgeRoute,
                longerRoute.subRoute(1, 2));

        Assert.assertEquals("Subroute the entire route", longerRoute,
                longerRoute.subRoute(0, longerRoute.size()));
    }
}

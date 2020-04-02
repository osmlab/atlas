package org.openstreetmap.atlas.geography.atlas.routing;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Route;

/**
 * {@link AllPathsRouter} unit test
 *
 * @author mgostintsev
 */
public class AllPathsRouterTest
{
    @Rule
    public RoutingTestRule rule = new RoutingTestRule();

    @Test
    public void testBidirectionalSimpleRoutes()
    {
        final Atlas atlas = this.rule.getBiDirectionalCyclicRouteAtlas();
        final Set<Route> routes = AllPathsRouter.allRoutes(atlas.edge(315932590),
                atlas.edge(317932590), Route.ROUTE_COMPARATOR);

        final Set<Route> expectedRoutes = new TreeSet<>(Route.ROUTE_COMPARATOR);
        expectedRoutes.add(Route.forEdges(atlas.edge(315932590), atlas.edge(316932590),
                atlas.edge(317932590)));
        expectedRoutes.add(Route.forEdges(atlas.edge(315932590), atlas.edge(-315932590),
                atlas.edge(-318932590), atlas.edge(-317932590), atlas.edge(-316932590),
                atlas.edge(316932590), atlas.edge(317932590)));
        expectedRoutes.add(Route.forEdges(atlas.edge(315932590), atlas.edge(316932590),
                atlas.edge(-316932590), atlas.edge(-315932590), atlas.edge(-318932590),
                atlas.edge(-317932590), atlas.edge(317932590)));
        expectedRoutes.add(Route.forEdges(atlas.edge(315932590), atlas.edge(-315932590),
                atlas.edge(-318932590), atlas.edge(-317932590), atlas.edge(317932590)));

        Assert.assertEquals("Expect four distinct routes between start and end", 4, routes.size());
        Assert.assertEquals("Expect deterministic results from the router", expectedRoutes, routes);
    }

    @Test
    public void testMultipleRoutesFromStartToEnd()
    {
        final Atlas atlas = this.rule.getMultipleRoutesAtlas();
        final Set<Route> routes = AllPathsRouter.allRoutes(atlas.edge(314932590),
                atlas.edge(319932590), Route.ROUTE_COMPARATOR);

        final Set<Route> expectedRoutes = new TreeSet<>(Route.ROUTE_COMPARATOR);
        expectedRoutes.add(Route.forEdges(atlas.edge(314932590), atlas.edge(315932590),
                atlas.edge(316932590), atlas.edge(319932590)));
        expectedRoutes.add(Route.forEdges(atlas.edge(314932590), atlas.edge(317932590),
                atlas.edge(318932590), atlas.edge(319932590)));

        Assert.assertEquals("Expect two distinct routes between start and end", 2, routes.size());
        Assert.assertEquals("Expect deterministic results from the router", expectedRoutes, routes);
    }

    @Test
    public void testNoPossibleRouteBetweenStartAndEnd()
    {
        final Atlas atlas = this.rule.getNoPossibleRouteAtlas();
        final Set<Route> routes = AllPathsRouter.allRoutes(atlas.edge(315932590),
                atlas.edge(318932590), Route.ROUTE_COMPARATOR);

        Assert.assertEquals("Expect no possible route between start and end", 0, routes.size());
    }

    @Test
    public void testRoutingAlongCyclicalGraph()
    {
        final Atlas atlas = this.rule.getCyclicalRouteAtlas();
        final Set<Route> routes = AllPathsRouter.allRoutes(atlas.edge(315932590),
                atlas.edge(317932590), Route.ROUTE_COMPARATOR);

        final Set<Route> expectedRoutes = new TreeSet<>(Route.ROUTE_COMPARATOR);
        expectedRoutes.add(Route.forEdges(atlas.edge(315932590), atlas.edge(316932590),
                atlas.edge(317932590)));

        Assert.assertEquals("Expect a single route between start and end", 1, routes.size());
        Assert.assertEquals("Expect deterministic results from the router", expectedRoutes, routes);
    }

    @Test
    public void testRoutingWithFilter()
    {
        final Atlas atlas = this.rule.getBiDirectionalCyclicRouteAtlas();
        final Predicate<Edge> onlyMasterEdges = edge -> Edge
                .isMasterEdgeIdentifier(edge.getIdentifier());
        final Set<Route> routes = AllPathsRouter.allRoutes(atlas.edge(315932590),
                atlas.edge(317932590), onlyMasterEdges, Route.ROUTE_COMPARATOR);

        final Set<Route> expectedRoutes = new TreeSet<>(Route.ROUTE_COMPARATOR);
        expectedRoutes.add(Route.forEdges(atlas.edge(315932590), atlas.edge(316932590),
                atlas.edge(317932590)));

        Assert.assertEquals(
                "Expect a single distinct route between start and end, since we've filtered out all routes that have a non-master edge",
                1, routes.size());
        Assert.assertEquals("Expect deterministic results from the router", expectedRoutes, routes);
    }

    @Test
    public void testRoutingWithFilterAndMaximumAllowedPath()
    {
        final Atlas atlas = this.rule.getMultipleRoutesAtlas();
        final Set<Route> routes = AllPathsRouter.allRoutes(atlas.edge(314932590),
                atlas.edge(319932590), x -> true, 1);

        final Set<Route> expectedRoutes = new HashSet<>();
        expectedRoutes.add(Route.forEdges(atlas.edge(314932590), atlas.edge(315932590),
                atlas.edge(316932590), atlas.edge(319932590)));

        Assert.assertEquals("Expect two distinct routes between start and end", 1, routes.size());
        Assert.assertEquals("Expect deterministic results from the router", expectedRoutes, routes);
    }

    @Test
    public void testSingleRouteFromStartToEnd()
    {
        final Atlas atlas = this.rule.getSingleRouteAtlas();
        final Set<Route> routes = AllPathsRouter.allRoutes(atlas.edge(315932590),
                atlas.edge(317932590), Route.ROUTE_COMPARATOR);

        final Set<Route> expectedRoutes = new TreeSet<>(Route.ROUTE_COMPARATOR);
        expectedRoutes.add(Route.forEdges(atlas.edge(315932590), atlas.edge(316932590),
                atlas.edge(317932590)));

        Assert.assertEquals("Expect a single route between start and end", 1, routes.size());
        Assert.assertEquals("Expect deterministic results from the router", expectedRoutes, routes);
        Assert.assertEquals(
                "Total number of edges in the Atlas equals the number of edges in the route",
                Route.forEdges(atlas.edges()), routes.iterator().next());
    }
}

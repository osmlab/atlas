package org.openstreetmap.atlas.geography.atlas.builder.store;

import static org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveRoute.ROUTE_SIZE_COMPARATOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;

/**
 * @author Sid
 */
public class AtlasPrimitiveRouteTest
{
    @Test
    public void testComparator()
    {
        final PolyLine polyline1 = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final PolyLine polyline2 = new PolyLine(Location.TEST_1, Location.TEST_7);
        final PolyLine polyline3 = new PolyLine(Location.TEST_7, Location.TEST_6);

        final AtlasPrimitiveEdge edge1 = new AtlasPrimitiveEdge(1, polyline1, new HashMap<>());
        final AtlasPrimitiveEdge edge2 = new AtlasPrimitiveEdge(2, polyline2, new HashMap<>());
        final AtlasPrimitiveEdge edge3 = new AtlasPrimitiveEdge(3, polyline3, new HashMap<>());

        final AtlasPrimitiveRoute routeOne = new AtlasPrimitiveRoute(Arrays.asList(edge1));
        final AtlasPrimitiveRoute routeTwo = new AtlasPrimitiveRoute(Arrays.asList(edge1, edge2));
        final AtlasPrimitiveRoute routeThree = new AtlasPrimitiveRoute(
                Arrays.asList(edge1, edge2, edge3));

        Assert.assertEquals("Compare same route should be 0", 0,
                ROUTE_SIZE_COMPARATOR.compare(routeOne, routeOne));
        Assert.assertEquals("Compare same route should be 0", 0,
                ROUTE_SIZE_COMPARATOR.compare(routeTwo, routeTwo));
        Assert.assertEquals("Compare same route should be 0", 0,
                ROUTE_SIZE_COMPARATOR.compare(routeThree, routeThree));

        Assert.assertEquals("Compare longer to shorter route should be -1", -1,
                ROUTE_SIZE_COMPARATOR.compare(routeThree, routeTwo));
        Assert.assertEquals("Compare shorter to longer route should be 1", 1,
                ROUTE_SIZE_COMPARATOR.compare(routeTwo, routeThree));

        final Set<AtlasPrimitiveRoute> atlasPrimitiveRoutes = new TreeSet<>(ROUTE_SIZE_COMPARATOR);
        atlasPrimitiveRoutes.add(routeOne);
        atlasPrimitiveRoutes.add(routeTwo);
        atlasPrimitiveRoutes.add(routeThree);

        int size = Integer.MAX_VALUE;
        for (final AtlasPrimitiveRoute route : atlasPrimitiveRoutes)
        {
            Assert.assertTrue("Set is sorted in descending order", size >= route.size());
            size = route.size();
        }
    }

    @Test
    public void testOverlap()
    {
        // We dont need anything more than ids for test edges
        final AtlasPrimitiveEdge edge1 = new AtlasPrimitiveEdge(1, null, new HashMap<>());
        final AtlasPrimitiveEdge edge2 = new AtlasPrimitiveEdge(2, null, new HashMap<>());
        final AtlasPrimitiveEdge edge3 = new AtlasPrimitiveEdge(3, null, new HashMap<>());
        final AtlasPrimitiveEdge edge4 = new AtlasPrimitiveEdge(4, null, new HashMap<>());
        final AtlasPrimitiveEdge edge5 = new AtlasPrimitiveEdge(5, null, new HashMap<>());
        final AtlasPrimitiveEdge edge6 = new AtlasPrimitiveEdge(6, null, new HashMap<>());

        final List<AtlasPrimitiveEdge> routeList = new ArrayList<>();
        routeList.add(edge1);
        routeList.add(edge2);
        routeList.add(edge3);
        routeList.add(edge4);
        routeList.add(edge2);
        routeList.add(edge3);
        routeList.add(edge4);
        routeList.add(edge5);

        final AtlasPrimitiveRoute route = new AtlasPrimitiveRoute(routeList);

        // First test - Multiple counts
        final List<AtlasPrimitiveEdge> subRouteList = new ArrayList<>();
        subRouteList.add(edge2);
        subRouteList.add(edge3);
        Assert.assertEquals("The number of overlaps for Multiple overlap subroute", 2,
                route.overlapCount(new AtlasPrimitiveRoute(subRouteList)));

        // Second test - Longer sub route
        subRouteList.clear();
        subRouteList.add(edge2);
        subRouteList.add(edge3);
        subRouteList.add(edge4);
        subRouteList.add(edge5);
        Assert.assertEquals("The number of overlaps for Longer subroute", 1,
                route.overlapCount(new AtlasPrimitiveRoute(subRouteList)));

        // Third test - empty
        subRouteList.clear();
        Assert.assertEquals("The number of overlaps for empty subroute", 0,
                route.overlapCount(new AtlasPrimitiveRoute(subRouteList)));

        // Fourth test - Non existent
        subRouteList.clear();
        subRouteList.add(edge4);
        subRouteList.add(edge5);
        subRouteList.add(edge6);
        Assert.assertEquals("The number of overlaps for Non existent subroute", 0,
                route.overlapCount(new AtlasPrimitiveRoute(subRouteList)));
    }
}

package org.openstreetmap.atlas.geography.atlas.routing;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/**
 * Router that returns all possible paths between two given {@link Edge}s, using DFS. Note: this is
 * very inefficient for larger {@link Atlas} files.
 *
 * @author mgostintsev
 */
public final class AllPathsRouter
{
    private static final Logger logger = LoggerFactory.getLogger(AllPathsRouter.class);
    private static final int MAXIMUM_ALLOWED_EDGES_FOR_TRAVERSAL = 1000;
    private static final int MAXIMUM_ALLOWED_PATHS = 20000;

    // The default filter to use when we want to include all edges.
    private static final Predicate<Edge> ALL_EDGES = edge -> true;

    /**
     * Find all possible routes from the start {@link Edge} to the end {@link Edge}, using DFS.
     *
     * @param start
     *            The {@link Edge} to start from
     * @param end
     *            The {@link Edge} to end at
     * @return all possible {@link Route}s from start to end
     */
    public static Set<Route> allRoutes(final Edge start, final Edge end)
    {
        if (start.getAtlas() != end.getAtlas())
        {
            throw new CoreException("Supplied start and end edges must come from the same atlas!");
        }
        if (Iterables.size(start.getAtlas().edges()) > MAXIMUM_ALLOWED_EDGES_FOR_TRAVERSAL)
        {
            throw new CoreException(
                    "Atlas has too many edges for an efficient traversal - aborting!");
        }

        final Stack<Edge> path = new Stack<>();
        final Set<Long> onPath = new HashSet<>();
        final Set<Route> routes = new HashSet<>();
        allRoutes(start, end, path, onPath, routes, ALL_EDGES);

        return routes;
    }

    /**
     * Find all possible routes from the start {@link Edge} to the end {@link Edge}, using DFS and
     * return in an order determined by the given {@link Comparator}.
     *
     * @param start
     *            The {@link Edge} to start from
     * @param end
     *            The {@link Edge} to end at
     * @param comparator
     *            Used to order the found routes
     * @return all possible {@link Route}s from start to end
     */
    public static Set<Route> allRoutes(final Edge start, final Edge end,
            final Comparator<Route> comparator)
    {
        if (start.getAtlas() != end.getAtlas())
        {
            throw new CoreException("Supplied start and end edges must come from the same atlas!");
        }
        if (Iterables.size(start.getAtlas().edges()) > MAXIMUM_ALLOWED_EDGES_FOR_TRAVERSAL)
        {
            throw new CoreException(
                    "Atlas has too many edges for an efficient traversal - aborting!");
        }

        final Stack<Edge> path = new Stack<>();
        final Set<Long> onPath = new HashSet<>();
        final Set<Route> routes = new TreeSet<>(comparator);
        allRoutes(start, end, path, onPath, routes, ALL_EDGES);

        return routes;
    }

    /**
     * Find all possible routes from the start {@link Edge} to the end {@link Edge}, using DFS. Only
     * {@link Edge}s that meet the given filter will be included in the resulting {@link Route}s.
     *
     * @param start
     *            The {@link Edge} to start from
     * @param end
     *            The {@link Edge} to end at
     * @param filter
     *            The filter to use when including {@link Edge}s that make up the route
     * @return all possible {@link Route}s from start to end
     */
    public static Set<Route> allRoutes(final Edge start, final Edge end,
            final Predicate<Edge> filter)
    {
        if (start.getAtlas() != end.getAtlas())
        {
            throw new CoreException("Supplied start and end edges must come from the same atlas!");
        }
        if (Iterables.size(start.getAtlas().edges()) > MAXIMUM_ALLOWED_EDGES_FOR_TRAVERSAL)
        {
            throw new CoreException(
                    "Atlas has too many edges for an efficient traversal - aborting!");
        }

        final Stack<Edge> path = new Stack<>();
        final Set<Long> onPath = new HashSet<>();
        final Set<Route> routes = new HashSet<>();
        allRoutes(start, end, path, onPath, routes, filter);

        return routes;
    }

    /**
     * Find all possible routes from the start {@link Edge} to the end {@link Edge}, using DFS. Only
     * {@link Edge}s that meet the given filter will be included in the resulting {@link Route}s.
     * The supplied {@link Comparator} will be used to order the {@link Route}s.
     *
     * @param start
     *            The {@link Edge} to start from
     * @param end
     *            The {@link Edge} to end at
     * @param filter
     *            The filter to use when including {@link Edge}s that make up the route
     * @param comparator
     *            Used to order the found routes
     * @return all possible {@link Route}s from start to end
     */
    public static Set<Route> allRoutes(final Edge start, final Edge end,
            final Predicate<Edge> filter, final Comparator<Route> comparator)
    {
        if (start.getAtlas() != end.getAtlas())
        {
            throw new CoreException("Supplied start and end edges must come from the same atlas!");
        }
        if (Iterables.size(start.getAtlas().edges()) > MAXIMUM_ALLOWED_EDGES_FOR_TRAVERSAL)
        {
            throw new CoreException(
                    "Atlas has too many edges for an efficient traversal - aborting!");
        }

        final Stack<Edge> path = new Stack<>();
        final Set<Long> onPath = new HashSet<>();
        final Set<Route> routes = new TreeSet<>(comparator);
        allRoutes(start, end, path, onPath, routes, filter);

        return routes;
    }

    private static void allRoutes(final Edge start, final Edge end, final Stack<Edge> path,
            final Set<Long> onPath, final Set<Route> routes, final Predicate<Edge> filter)
    {
        if (routes.size() > MAXIMUM_ALLOWED_PATHS)
        {
            return;
        }
        // Add start edge to the path
        path.push(start);
        onPath.add(start.getIdentifier());

        if (start.equals(end))
        {
            // Found a path from start to end, save this route
            routes.add(Route.forEdges(path));

            if (routes.size() > MAXIMUM_ALLOWED_PATHS)
            {
                logger.warn("Too many paths found - aborting! Path so far: {}",
                        path.stream().map(edge -> String.valueOf(edge.getIdentifier()))
                                .collect(Collectors.toList()));
            }
        }
        else
        {
            // Consider all outgoing, non-zero length edges that can continue the current path,
            // without repeating an edge that's already part of the current path, and that meet the
            // given filter
            for (final Edge candidate : start.outEdges())
            {
                if (!candidate.isZeroLength() && !onPath.contains(candidate.getIdentifier())
                        && (filter.test(candidate) || candidate.equals(end)))
                {
                    allRoutes(candidate, end, path, onPath, routes, filter);
                }
            }
        }

        // We've explored all paths that go through this edge. Remove it from consideration
        path.pop();
        onPath.remove(start.getIdentifier());
    }

    private AllPathsRouter()
    {
    }

}

package org.openstreetmap.atlas.geography.atlas.items;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A route is a set of {@link Edge}s that are connected to each other.
 *
 * @author matthieun
 * @author Sid
 * @author mgostintsev
 */
@SuppressWarnings("serial")
public abstract class Route implements Iterable<Edge>, Located, Serializable
{
    /**
     * A {@link Route} implementation made of many {@link Edge}s.
     *
     * @author matthieun
     */
    private static final class MultiRoute extends Route
    {
        private static final long serialVersionUID = -4562811506650155750L;
        private final Route upstream;
        private final Route downstream;

        private MultiRoute(final Route upstream, final Route downstream)
        {
            this.upstream = upstream;
            this.downstream = downstream;
        }

        @Override
        public PolyLine asPolyLine()
        {
            final PolyLine one = this.upstream.asPolyLine();
            final PolyLine two = this.downstream.asPolyLine();
            final List<Location> points = new ArrayList<>();
            one.forEach(point -> points.add(point));
            for (int i = 1; i < two.size(); i++)
            {
                points.add(two.get(i));
            }
            return new PolyLine(points);
        }

        @Override
        public Rectangle bounds()
        {
            return Rectangle.forLocated(this.upstream, this.downstream);
        }

        @Override
        public Edge end()
        {
            return this.downstream.end();
        }

        @Override
        public Edge get(final int index)
        {
            final int size = size();
            if (index < 0 || index >= size)
            {
                throw new CoreException("Index {} out of Route's bounds: size = {}", index, size);
            }
            else
            {
                final int upstreamSize = this.upstream.size();
                if (index < upstreamSize)
                {
                    return this.upstream.get(index);
                }
                else
                {
                    return this.downstream.get(index - upstreamSize);
                }
            }
        }

        @Override
        public int indexOf(final Edge edge)
        {
            final int indexUp = this.upstream.indexOf(edge);
            if (indexUp >= 0)
            {
                return indexUp;
            }
            final int indexDown = this.downstream.indexOf(edge);
            if (indexDown >= 0)
            {
                return this.upstream.size() + indexDown;
            }
            return indexDown;
        }

        @Override
        public Iterator<Edge> iterator()
        {
            return new MultiIterable<>(this.upstream, this.downstream).iterator();
        }

        @Override
        public Distance length()
        {
            return this.upstream.length().add(this.downstream.length());
        }

        @Override
        public List<Node> nodes()
        {
            final List<Node> nodes = new ArrayList<>();
            nodes.addAll(this.upstream.nodes());
            final List<Node> downNodes = this.downstream.nodes();
            for (int i = 1; i < downNodes.size(); i++)
            {
                nodes.add(downNodes.get(i));
            }
            return nodes;
        }

        @Override
        public Optional<Route> reverse()
        {
            Route reversed = null;
            final Iterator<Edge> iterator = this.iterator();
            while (iterator.hasNext())
            {
                final Edge edge = iterator.next();
                if (edge.hasReverseEdge())
                {
                    final Edge reverse = edge.reversed().get();
                    if (reversed == null)
                    {
                        reversed = Route.forEdge(reverse);
                    }
                    else
                    {
                        reversed = reversed.prepend(reverse);
                    }
                }
                else
                {
                    return Optional.empty();
                }
            }
            return Optional.ofNullable(reversed);
        }

        @Override
        public int size()
        {
            return this.upstream.size() + this.downstream.size();
        }

        @Override
        public Edge start()
        {
            return this.upstream.start();
        }
    }

    /**
     * A {@link Route} made of a single {@link Edge}
     *
     * @author matthieun
     */
    private static final class SingleRoute extends Route
    {
        private static final long serialVersionUID = -3870416343539125425L;
        private final Edge edge;

        SingleRoute(final Edge edge)
        {
            this.edge = edge;
        }

        @Override
        public PolyLine asPolyLine()
        {
            return this.edge.asPolyLine();
        }

        @Override
        public Rectangle bounds()
        {
            return this.edge.bounds();
        }

        @Override
        public Edge end()
        {
            return this.edge;
        }

        @Override
        public Edge get(final int index)
        {
            if (index != 0)
            {
                throw new CoreException("Invalid SingleRoute index: {}. Only 0 is permitted.",
                        index);
            }
            return this.edge;
        }

        @Override
        public int indexOf(final Edge edge)
        {
            return this.edge.getIdentifier() == edge.getIdentifier() ? 0 : -1;
        }

        @Override
        public Iterator<Edge> iterator()
        {
            return new Iterator<Edge>()
            {
                private int index = 0;

                @Override
                public boolean hasNext()
                {
                    return this.index < 1;
                }

                @Override
                public Edge next()
                {
                    if (this.index > 0)
                    {
                        return null;
                    }
                    this.index++;
                    return SingleRoute.this.edge;
                }
            };
        }

        @Override
        public Distance length()
        {
            return this.edge.length();
        }

        @Override
        public List<Node> nodes()
        {
            final List<Node> result = new ArrayList<>();
            result.add(this.edge.start());
            result.add(this.edge.end());
            return result;
        }

        @Override
        public Optional<Route> reverse()
        {
            return this.edge.hasReverseEdge()
                    ? Optional.of(new SingleRoute(this.edge.reversed().get())) : Optional.empty();
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public Edge start()
        {
            return this.edge;
        }
    }

    /**
     * Comparator that sorts {@link Route}s from longest to shortest and then by individual
     * {@link Route} hashCode.
     */
    public static final Comparator<Route> ROUTE_COMPARATOR = (final Route route1,
            final Route route2) -> new CompareToBuilder().append(route2.size(), route1.size())
                    .append(route1.hashCode(), route2.hashCode()).toComparison();

    private static final Logger logger = LoggerFactory.getLogger(Route.class);

    /**
     * Given a set of {@link Edge}s, which may or may not have reverse {@link Edge}s, build a
     * {@link Route} that uses each unique {@link Edge} exactly once. Throws an exception if it
     * cannot build a {@link Route}.
     *
     * @param candidates
     *            The {@link Edge}s
     * @param startNode
     *            Starting {@link Node} of the {@link Route}
     * @param endNode
     *            Ending {@link Node} of the {@link Route}
     * @return The corresponding {@link Route}.
     */
    public static Route buildFullRouteIgnoringReverseEdges(final Set<Edge> candidates,
            final Node startNode, final Node endNode)
    {
        Route route = null;
        int numberOfConsecutiveFailures = 0;
        final long maxEdgesToAdd = candidates.stream().map(edge -> edge.getMasterEdgeIdentifier())
                .distinct().count();
        final Set<Long> idsAdded = new HashSet<>();
        if (maxEdgesToAdd == 0)
        {
            throw new CoreException("Can't have a route with no members");
        }
        while (route == null || route.size() < maxEdgesToAdd
                && !(route.start().start().equals(startNode) && route.end().end().equals(endNode)))
        {
            if (route == null)
            {
                // Find an edge that connects to the startNode
                for (final Edge edge : candidates)
                {
                    if (edge.start().equals(startNode))
                    {
                        route = Route.forEdge(edge);
                        idsAdded.add(edge.getMasterEdgeIdentifier());
                        break;
                    }
                }
                if (route == null)
                {
                    throw new CoreException(
                            "Can't find an edge that connects to the startNode. StartNode: {} EndNode: {}",
                            startNode.getIdentifier(), endNode.getIdentifier());
                }
            }
            else
            {
                boolean edgeAdded = false;

                for (final Edge edge : candidates)
                {
                    if (idsAdded.contains(edge.getMasterEdgeIdentifier()))
                    {
                        // this edge or reverseEdge is already used, continue
                        continue;
                    }
                    // Can use equals here, as the items all come from the same atlas.
                    // Note: Here the order has a great importance. It is edge start to route
                    // end before edge end to route start, otherwise all the self-intersecting
                    // osm ways will not be able to create a route. In the case of MultiAtlas
                    // re-creating ways that have been mis-way-sectioned at borders, this is because
                    // the edges are sorted in ascending order and processed here in the same order.
                    if (edge.start().equals(route.end().end()))
                    {
                        edgeAdded = true;
                        numberOfConsecutiveFailures = 0;
                        route = route.append(edge);
                        idsAdded.add(edge.getMasterEdgeIdentifier());
                        break;
                    }
                }

                // To ensure there's no infinite loop, number of consecutive loops where an edge is
                // not added cannot exceed the total number of unique edges passed in
                if (!edgeAdded)
                {
                    if (++numberOfConsecutiveFailures >= maxEdgesToAdd)
                    {
                        throw new CoreException(
                                "No edge that connects to the current route. StartNode: {} EndNode: {}",
                                startNode.getIdentifier(), endNode.getIdentifier());
                    }
                }
            }
        }

        if (route.size() != maxEdgesToAdd)
        {
            throw new CoreException(
                    "A route was found from start to end, but not every unique edge was used. StartNode: {} EndNode: {}",
                    startNode.getIdentifier(), endNode.getIdentifier());
        }

        return route;
    }

    /**
     * Create a {@link Route} from a single {@link Edge}
     *
     * @param edge
     *            The {@link Edge}
     * @return The single-{@link Edge} {@link Route}
     */
    public static Route forEdge(final Edge edge)
    {
        if (edge == null)
        {
            throw new CoreException("Cannot create a Route from a null Edge.");
        }
        return new SingleRoute(edge);
    }

    /**
     * Create a {@link Route} from an {@link Iterable} of {@link Edge}s that are already in the
     * proper order to be connected.
     *
     * @param edges
     *            The {@link Edge}s to link in a {@link Route}
     * @return The corresponding {@link Route}
     */
    public static Route forEdges(final Edge... edges)
    {
        return forEdges(Iterables.asList(edges));
    }

    /**
     * Create a {@link Route} from an {@link Iterable} of {@link Edge}s that are already in the
     * proper order to be connected.
     *
     * @param edges
     *            The {@link Edge}s to link in a {@link Route}
     * @return The corresponding {@link Route}
     */
    public static Route forEdges(final Iterable<Edge> edges)
    {
        if (!edges.iterator().hasNext())
        {
            throw new CoreException("Cannot have no edges");
        }
        int counter = 0;
        Route result = null;
        for (final Edge edge : edges)
        {
            if (counter == 0)
            {
                result = Route.forEdge(edge);
            }
            else
            {
                result = result.append(edge);
            }
            counter++;
        }
        return result;
    }

    public static Route forRoutes(final Iterable<Route> routes)
    {
        if (!routes.iterator().hasNext())
        {
            throw new CoreException("Cannot have no edges");
        }
        Route result = null;
        for (final Route route : routes)
        {
            if (result == null)
            {
                result = route;
            }
            else
            {
                result = result.append(route);
            }
        }
        return result;
    }

    public static Route forRoutes(final Route... routes)
    {
        return forRoutes(Iterables.asList(routes));
    }

    /**
     * Get a {@link Route} from a set of {@link Edge}s, that we assume are connected. However, this
     * does not require the {@link Edge}s to be in any order. The order should be inferred by this
     * method. Throws an exception if it cannot build a {@link Route}.
     *
     * @param candidates
     *            The {@link Edge}s
     * @param shuffle
     *            When no {@link Route} is found on the first pass, if this is true, the set of
     *            {@link Edge}s will be shuffled to find routes that might have been missed. This is
     *            way slower.
     * @return The corresponding {@link Route}.
     */
    public static Route fromNonArrangedEdgeSet(final Set<Edge> candidates, final boolean shuffle)
    {
        Route route = null;
        int numberFailures = 0;
        final List<Edge> members = new ArrayList<>();
        members.addAll(candidates);
        if (members.size() == 0)
        {
            throw new CoreException("Cannot have a route with no members");
        }
        while (route == null || route.size() < members.size())
        {
            if (route == null)
            {
                route = Route.forEdge(members.iterator().next());
            }
            else
            {
                final int initialSize = route.size();
                for (final Edge edge : members)
                {
                    if (route.includes(edge))
                    {
                        // this edge is already used, continue
                        continue;
                    }
                    // Can use equals here, as the items all come from the same atlas.
                    // Note: Here the order has a great importance. It is edge start to route
                    // end before edge end to route start, otherwise all the self-intersecting
                    // osm ways will not be able to create a route. In the case of MultiAtlas
                    // re-creating ways that have been mis-way-sectioned at borders, this is because
                    // the edges are sorted in ascending order and processed here in the same order.
                    if (edge.start().equals(route.end().end()))
                    {
                        route = route.append(edge);
                        break;
                    }
                    if (edge.end().equals(route.start().start()))
                    {
                        route = route.prepend(edge);
                        break;
                    }
                }
                if (initialSize + 1 != route.size())
                {
                    if (shuffle && ++numberFailures < candidates.size())
                    {
                        // The user suggested that the algorithm is sensitive to which edge is the
                        // first in case of loops
                        // Try another first edge
                        final Edge firstMember = members.remove(0);
                        members.add(firstMember);
                        // Make the loop restart
                        route = null;
                    }
                    else
                    {
                        // Format and throw an exception.
                        final StringList edges = new StringList();
                        final StringList debug = new StringList();
                        candidates.forEach(edge ->
                        {
                            edges.add(edge.getIdentifier());
                        });
                        candidates.forEach(edge ->
                        {
                            debug.add(edge.asPolyLine().toWkt());
                        });
                        throw new CoreException(
                                "Unable to build a route from edges {}\nLocations:\n{}",
                                edges.join(", "), debug.join("\n"));
                    }
                }
            }
        }
        return route;
    }

    protected Route()
    {
    }

    public Route append(final Edge edge)
    {
        return append(Route.forEdge(edge));
    }

    public Route append(final Route route)
    {
        if (route == null)
        {
            throw new CoreException(
                    "Cannot append a route that is null to a route {} that ends at {}", this,
                    this.end());
        }
        if (!end().end().equals(route.start().start()))
        {
            throw new CoreException(
                    "Cannot append a disconnected route:\nOne: {}\nAt: {}\nTo\nTwo: {}\nAt: {}",
                    this, this.end(), route, route.start());
        }
        return new MultiRoute(this, route);
    }

    public abstract PolyLine asPolyLine();

    /**
     * @return All the {@link Edge}s connected to the start/end {@link Node}s of this {@link Route},
     *         excluding {@link Edge}s in {@link Route}. If this {@link Edge} is a {@link Route}
     *         with two-way road, then the reversed {@link Edge}s will be included in the set. This
     *         does not include {@link Edge} connected to the interior {@link Node}s of the
     *         {@link Route}.
     */
    public Set<Edge> connectedEdges()
    {
        final Set<Edge> result = new HashSet<>();
        for (final Edge edge : this.end().end().connectedEdges())
        {
            if (!this.includes(edge))
            {
                result.add(edge);
            }
        }
        for (final Edge edge : this.start().start().connectedEdges())
        {
            if (!this.includes(edge))
            {
                result.add(edge);
            }
        }
        return result;
    }

    public abstract Edge end();

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Route)
        {
            final Route that = (Route) other;
            if (this.size() == that.size())
            {
                return new EqualsBuilder()
                        .append(this.start().start().getLocation(),
                                that.start().start().getLocation())
                        .append(this.end().end().getLocation(), that.end().end().getLocation())
                        .append(Iterables.asList(this), Iterables.asList(that)).isEquals();
            }
        }
        return false;
    }

    /**
     * @param index
     *            An index in the {@link Route}
     * @return The {@link Edge} at the specified index in the {@link Route}
     */
    public abstract Edge get(int index);

    /**
     * Note: The start and end {@link Node}s of the {@link Route} are part of the hash code to
     * reduce the probability of a collision. There are other candidates to add here, like distance
     * between start/end, but start/end by themselves are the least computationally intensive to
     * derive.
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.start().start().getLocation())
                .append(this.end().end().getLocation()).append(Iterables.asList(this)).hashCode();
    }

    /**
     * @param edge
     *            The {@link Edge} to test for
     * @return true if the {@link Edge} provided belongs in the {@link Route}
     */
    public boolean includes(final Edge edge)
    {
        return this.indexOf(edge) >= 0;
    }

    /**
     * @param edge
     *            The {@link Edge} to test for
     * @return The index of the {@link Edge} in the {@link Route} if it is included, -1 otherwise.
     */
    public abstract int indexOf(Edge edge);

    /**
     * Identifies whether the entire given {@link Route} is overlapping.
     *
     * @param route
     *            The {@link Route} to check
     * @return true if the given {@link Route} is overlapping
     */
    public boolean isOverlapping(final Route route)
    {
        return overlapIndex(route) > -1;
    }

    /**
     * Identifies whether any of the given {@link Route} is entirely overlapping this one.
     *
     * @param routes
     *            The {@link Iterable} of {@link Route}s to check
     * @return true if any of the given {@link Route}s is overlapping
     */
    public boolean isOverlappingForAtLeastOneOf(final Iterable<Route> routes)
    {
        return overlapIndex(routes) > -1;
    }

    /**
     * Identifies whether this @{link Route} is a simple U-Turn (route follows along a path to a
     * point and returns the exact same way it came in).
     * <p>
     * NOTE: A route could still be a U-Turn that doesn't follow an identical path out based on
     * {@link Heading}, but this method won't catch that case.
     *
     * @return true if this route is a simple U-Turn
     */
    public boolean isSimpleUTurn()
    {
        final int numberOfEdges = this.size();

        // A simple UTurn route cannot have an odd number of edges
        if (numberOfEdges % 2 == 1)
        {
            return false;
        }

        int index = 0;

        // Start by comparing the first and last edge, and incrementally move in from each side.
        // Stop after we compare the middle two edges
        while (index < numberOfEdges / 2)
        {
            // If any comparison doesn't match, it's not a simple U-Turn
            if (!this.get(index).isReversedEdge(this.get(numberOfEdges - index - 1)))
            {
                return false;
            }

            index++;
        }

        // If the comparisons all succeeded, it's a simple U-Turn!
        return true;
    }

    /**
     * Identifies whether the given {@link Route} is a sub-route of this one. If the given
     * {@link Route} is greater than this {@link Route}, this method will return false.
     *
     * @param route
     *            The {@link Route} to check
     * @return true if the given {@link Route} is a sub-route
     */
    public boolean isSubRoute(final Route route)
    {
        return subRouteIndex(route) > -1;
    }

    /**
     * Identifies whether any of the given {@link Route} is a sub-route of this one.
     *
     * @param routes
     *            The {@link Iterable} of {@link Route}s to check
     * @return true if any of the given {@link Route}s is a sub-route.
     */
    public boolean isSubRouteForAtLeastOneOf(final Iterable<Route> routes)
    {
        return subRouteIndex(routes) > -1;
    }

    /**
     * @return true if this {@link Route} contains a Turn Restriction given OSM's definition.
     */
    public boolean isTurnRestriction()
    {
        return TurnRestriction.isTurnRestriction(this);
    }

    public abstract Distance length();

    public abstract List<Node> nodes();

    /**
     * Calculates the first occurring overlapping index from the given {@link Route}s. For details,
     * see {@link #overlapIndex(Route)}.
     *
     * @param routes
     *            The {@link Route}s to compare with
     * @return first occurring calculated index
     */
    public int overlapIndex(final Iterable<Route> routes)
    {
        for (final Route route : routes)
        {
            final int overlapIndex = overlapIndex(route);
            if (overlapIndex > -1)
            {
                return overlapIndex;
            }
        }

        return -1;
    }

    /**
     * Calculates the index of the last {@link Edge} from this {@link Route} that overlaps the given
     * {@link Route}. If there is no overlap, -1 is returned. Note: The given {@link Route} can be
     * of any size. Example 1 - this route: [A,B], given route: [A,B,C] will return 1 since the last
     * overlap occurs at Edge B, index 1 for this route. Example 2 - this route: [A,B,C], given
     * route: [C] will return 2, since overlap is at C, index 2 for this route.
     *
     * @param route
     *            The {@link Route} to compare with
     * @return the calculated index
     */
    public int overlapIndex(final Route route)
    {
        int overlapIndex;
        boolean givenPathIsLonger = false;

        // Find overlap index relative to this route, but use the longer of the two routes to find
        // the overlap section
        if (route.size() > this.size())
        {
            givenPathIsLonger = true;
            overlapIndex = route.subRouteIndex(this);
        }
        else
        {
            overlapIndex = subRouteIndex(route);
        }

        // If there is an overlap and the given route was longer, go back and find the index for the
        // overlapping edge in this route
        if (givenPathIsLonger && overlapIndex > -1)
        {
            final Edge lastOverlap = route.get(overlapIndex);
            if (this.includes(lastOverlap))
            {
                return this.indexOf(lastOverlap);
            }

            logger.error("Detected overlap at edge {}, but unable to find in current route {}",
                    lastOverlap.getIdentifier(), this.toString());

            overlapIndex = -1;
        }

        return overlapIndex;
    }

    public Route prepend(final Edge edge)
    {
        return prepend(Route.forEdge(edge));
    }

    public Route prepend(final Route route)
    {
        if (!start().start().equals(route.end().end()))
        {
            throw new CoreException("Cannot prepend a disconnected route.");
        }
        return new MultiRoute(route, this);
    }

    /**
     * @return The reversed {@link Route}, if all the reversed {@link Edge}s exist.
     */
    public abstract Optional<Route> reverse();

    /**
     * @return The number of {@link Edge}s in this {@link Route}
     */
    public abstract int size();

    /**
     * @return The first {@link Edge} in this route
     */
    public abstract Edge start();

    /**
     * Determine if this route starts with the {@link Route} passed in.
     *
     * @param other
     *            The {@link Route} to compare
     * @return true if this route starts with the route passed in
     */
    public boolean startsWith(final Route other)
    {
        // If the other route is longer than this route, return false
        if (other.size() > this.size())
        {
            return false;
        }

        final Iterator<Edge> otherIterator = other.iterator();
        final Iterator<Edge> thisIterator = this.iterator();

        // otherIterator has to be shorter than thisIterator, so loop over otherIterator
        while (otherIterator.hasNext())
        {
            final Edge otherEdge = otherIterator.next();
            final Edge thisEdge = thisIterator.next();

            if (!thisEdge.equals(otherEdge))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates a new {@link Route} from the original route based on the start and end indexes passed
     * in
     *
     * @param startIndex
     *            The starting index to create the new route from
     * @param endIndex
     *            The ending index (exclusive) of the new route
     * @return a new route based which is a subset of the original route
     */
    public Route subRoute(final int startIndex, final int endIndex)
    {
        // Create a new ArrayList for safety reasons because subList returns a list backed by the
        // original list.
        return Route
                .forEdges(new ArrayList<>(Iterables.asList(this).subList(startIndex, endIndex)));
    }

    /**
     * Calculates the first occurring subRoute index from the given {@link Route}s. For details, see
     * {@link #subRouteIndex(Route)}.
     *
     * @param routes
     *            The {@link Route}s to compare with
     * @return first occurring calculated index
     */
    public int subRouteIndex(final Iterable<Route> routes)
    {
        for (final Route route : routes)
        {
            final int overlapIndex = subRouteIndex(route);
            if (overlapIndex > -1)
            {
                return overlapIndex;
            }
        }

        return -1;
    }

    /**
     * Calculates the index of the last {@link Edge} from this {@link Route} that overlaps the given
     * {@link Route}. If there is no overlap, -1 is returned. Note: The given {@link Route} must be
     * shorter than or equal to this {@link Route}. Example: This route: [A,B]. Given route: [A,B,C]
     * will return -1 since given route goes beyond this route. To avoid the size constraint, and
     * detect any overlap, use {@link #overlapIndex(Route)} instead.
     *
     * @param route
     *            The {@link Route} to compare with
     * @return the calculated index
     */
    public int subRouteIndex(final Route route)
    {
        // Keep track of the last index at which the last Edge was overlapping this route, to avoid
        // returning false positives in case of routes making a loop.
        int lastOverlapIndex = -1;

        // Fail-fast optimization
        if (route == null || route.size() > this.size())
        {
            return -1;
        }

        for (final Edge routeEdge : route)
        {
            final int index = this.indexOf(routeEdge);
            if (index <= lastOverlapIndex)
            {
                // The edge does not overlap, or it does but at a smaller index which would indicate
                // a loop.
                return -1;
            }
            lastOverlapIndex = index;
        }
        return lastOverlapIndex;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[Route: ");
        final StringList edgeIdentifiers = new StringList();
        this.forEach(edge -> edgeIdentifiers.add(String.valueOf(edge.getIdentifier())));
        builder.append(edgeIdentifiers.join(", "));
        builder.append("]");
        return builder.toString();
    }
}

package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.common.collect.ImmutableList;

/**
 * @author Sid
 */
public final class AtlasPrimitiveRoute implements Iterable<AtlasPrimitiveEdge>, Serializable
{
    private static final long serialVersionUID = 804872809334744220L;

    /*
     * This comparator can be used to sort AtlasPrimitiveRoutes in descending order of size.
     */
    public static final Comparator<AtlasPrimitiveRoute> ROUTE_SIZE_COMPARATOR = (
            final AtlasPrimitiveRoute route1,
            final AtlasPrimitiveRoute route2) -> new CompareToBuilder()
                    .append(route2.size(), route1.size())
                    // The hashcode needs to be deterministic to ensure deterministic order
                    .append(route1.hashCode(), route2.hashCode()).toComparison();

    private final List<AtlasPrimitiveEdge> primitiveRoute;

    public static AtlasPrimitiveRoute from(final AtlasPrimitiveEdge... primitiveEdges)
    {
        return new AtlasPrimitiveRoute(Arrays.asList(primitiveEdges));
    }

    public static AtlasPrimitiveRoute from(
            final AtlasPrimitiveRouteIdentifier atlasPrimitiveRouteIdentifier, final Atlas atlas)
    {
        final List<Edge> fromEdges = Iterables.stream(atlasPrimitiveRouteIdentifier)
                .map(edgeIdentifier -> atlas.edge(edgeIdentifier.getIdentifier())).collectToList();
        return AtlasPrimitiveRoute.from(fromEdges);
    }

    public static AtlasPrimitiveRoute from(final List<Edge> edges)
    {
        return new AtlasPrimitiveRoute(
                edges.stream().map(AtlasPrimitiveEdge::from).collect(Collectors.toList()));
    }

    public static Optional<AtlasPrimitiveRoute> from(final Route route)
    {
        if (route != null && route.size() > 0)
        {
            return Optional.of(
                    new AtlasPrimitiveRoute(Iterables.translate(route, AtlasPrimitiveEdge::from)));
        }
        return Optional.empty();
    }

    public AtlasPrimitiveRoute(final Iterable<AtlasPrimitiveEdge> primitiveEdges)
    {
        this.primitiveRoute = ImmutableList.copyOf(primitiveEdges);
    }

    public PolyLine asPolyLine()
    {
        final List<Location> locations = new ArrayList<>();
        Location lastLocation = null;
        for (final AtlasPrimitiveEdge edge : this.primitiveRoute)
        {
            final PolyLine polyLine = edge.getPolyLine();
            locations.addAll(polyLine);
            lastLocation = polyLine.last();
            locations.remove(lastLocation);
        }
        locations.add(lastLocation);
        return new PolyLine(locations);
    }

    public AtlasPrimitiveEdge end()
    {
        if (this.primitiveRoute.size() > 0)
        {
            return this.primitiveRoute.get(this.primitiveRoute.size() - 1);
        }
        throw new CoreException("Illegal State : Empty route");
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof AtlasPrimitiveRoute)
        {
            final AtlasPrimitiveRoute that = (AtlasPrimitiveRoute) other;
            if (this.size() == that.size())
            {
                return new EqualsBuilder().append(this.start().start(), that.start().start())
                        .append(this.end().end(), that.end().end())
                        .append(this.primitiveRoute, that.primitiveRoute).isEquals();
            }
        }
        return false;
    }

    /**
     * This method uses the provided {@link Atlas} to return the {@link Route} corresponding to this
     * {@link AtlasPrimitiveRoute}
     *
     * @param atlas
     *            {@link Atlas} containing the {@link Route} {@link Edge}s
     * @return {@link Route} corresponding to this {@link AtlasPrimitiveRoute}
     */
    public Optional<Route> getRoute(final Atlas atlas)
    {
        final List<Edge> edges = new ArrayList<>();
        for (final AtlasPrimitiveEdge primitiveEdge : this.primitiveRoute)
        {
            final Edge edge = atlas.edge(primitiveEdge.getIdentifier());
            if (edge == null)
            {
                return Optional.empty();
            }
            edges.add(edge);
        }
        return Optional.of(Route.forEdges(edges));
    }

    /**
     * Note: The start and end {@link Node}s are part of the hash code to reduce the probability of
     * a collision. There are other candidates to add here, like distance between start/end, but
     * start/end by themselves are the least computationally intensive to derive. For best practice,
     * this is consistent with {@link Route}'s hash code.
     */
    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.start().start()).append(this.end().end())
                .append(Iterables.asList(this)).hashCode();
    }

    public int indexOf(final AtlasPrimitiveEdge primitiveEdge)
    {
        return this.primitiveRoute.indexOf(primitiveEdge);
    }

    public boolean isOverlappedBy(final AtlasPrimitiveRoute primitiveRoute)
    {
        return AtlasPrimitiveRouteIdentifier.from(this)
                .isOverlappedBy(AtlasPrimitiveRouteIdentifier.from(primitiveRoute));
    }

    public boolean isOverlappedBy(final Route route)
    {
        final Optional<AtlasPrimitiveRoute> atlasPrimitiveRoute = AtlasPrimitiveRoute.from(route);
        return atlasPrimitiveRoute.isPresent() && isOverlappedBy(atlasPrimitiveRoute.get());
    }

    @Override
    public Iterator<AtlasPrimitiveEdge> iterator()
    {
        return this.primitiveRoute.iterator();
    }

    /**
     * Counts the number of times a subRoute overlaps a route. Currently this doesn't handle cases
     * with loops within subRoute E.g [1,2,3,1,2,3,1,4,5], subRoute is [1,2,3,1,4]
     *
     * @param subRoute
     *            Smaller subsequence of the route that can overlap with route
     * @return overlapCount
     */
    public int overlapCount(final AtlasPrimitiveRoute subRoute)
    {
        int overlapCount = 0;
        if (this.primitiveRoute == null || subRoute == null)
        {
            return overlapCount;
        }
        Iterator<AtlasPrimitiveEdge> subRouteIterator = subRoute.iterator();
        AtlasPrimitiveEdge subRouteEdge = subRouteIterator.hasNext() ? subRouteIterator.next()
                : null;
        for (final AtlasPrimitiveEdge edge : this.primitiveRoute)
        {
            if (subRouteEdge == null)
            {
                break;
            }
            if (edge.equals(subRouteEdge))
            {
                if (!subRouteIterator.hasNext())
                {
                    overlapCount++;
                    subRouteIterator = subRoute.iterator();
                }
            }
            else
            {
                subRouteIterator = subRoute.iterator();
                if (edge.equals(subRoute.start()))
                {
                    subRouteEdge = subRouteIterator.hasNext() ? subRouteIterator.next() : null;
                }
            }
            subRouteEdge = subRouteIterator.hasNext() ? subRouteIterator.next() : null;
        }
        return overlapCount;
    }

    public int size()
    {
        return this.primitiveRoute.size();
    }

    public AtlasPrimitiveEdge start()
    {
        if (this.primitiveRoute.size() > 0)
        {
            return this.primitiveRoute.get(0);
        }
        throw new CoreException("Illegal State : Empty route");
    }

    public List<AtlasPrimitiveEdge> subRoute(final int fromIndex, final int toIndex)
    {
        return this.primitiveRoute.subList(fromIndex, toIndex);
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[AtlasPrimitiveRoute: ");
        builder.append(this.primitiveRoute.stream().map(edge -> edge.toString())
                .collect(Collectors.joining(", ")));
        builder.append("]");
        return builder.toString();
    }
}

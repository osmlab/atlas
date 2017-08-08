package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import java.util.NavigableSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Route;

/**
 * Since the constructor for ComplexHighwayArea must pass the source edge immediately we need to do
 * all of the processing of routes and which edges are the lowest ordered ones outside of that
 * class.
 *
 * @author cstaylor
 */
class ComplexHighwayAreaHelper
{
    private final NavigableSet<Long> visitedEdgeIdentifiers = new TreeSet<>();

    private PolyLine boundary;

    private Edge sourceEdge;

    private CoreException oops;

    ComplexHighwayAreaHelper(final Edge edge)
    {
        this.sourceEdge = edge;

        buildHighwayAreaBoundary(Route.forEdge(edge)).ifPresent(route ->
        {
            this.boundary = route.asPolyLine();
            StreamSupport.stream(route.spliterator(), false).map(Edge::getIdentifier)
                    .forEach(this.visitedEdgeIdentifiers::add);
            this.sourceEdge = edge.getAtlas().edge(this.visitedEdgeIdentifiers.first());
        });
        if (this.boundary == null)
        {
            this.oops = new CoreException("Unable to build boundary for edge {}",
                    edge.getOsmIdentifier());
        }
    }

    PolyLine getBoundary()
    {
        return this.boundary;
    }

    CoreException getException()
    {
        return this.oops;
    }

    Edge getSourceEdge()
    {
        return this.sourceEdge;
    }

    NavigableSet<Long> getVisitedEdgeIdentifiers()
    {
        return this.visitedEdgeIdentifiers;
    }

    private Optional<Route> buildHighwayAreaBoundary(final Route boundary)
    {
        for (final Edge edge : boundary.end().end().connectedEdges())
        {
            if (canAddEdgeToBoundary(edge, boundary))
            {
                final Route extendedBoundary = boundary.append(edge);
                if (extendedBoundary.end().end().getLocation()
                        .equals(extendedBoundary.start().start().getLocation()))
                {
                    return Optional.of(extendedBoundary);
                }
                else
                {
                    return buildHighwayAreaBoundary(extendedBoundary);
                }
            }
        }
        return Optional.empty();
    }

    // 1. Traversing in one direction, don't add any reverse edges
    // 2. There are some overlapping areas (bad data) which represent the same entity. To avoid
    // adding incorrect edges, only add edges with the same OSM identifier.
    // 3. The end location of the boundary matches the start location of the candidate edge.
    // 4. No duplicate edges.
    private boolean canAddEdgeToBoundary(final Edge edge, final Route boundary)
    {
        return edge.getIdentifier() != -boundary.end().getIdentifier()
                && edge.getOsmIdentifier() == boundary.end().getOsmIdentifier()
                && boundary.end().end().getLocation().equals(edge.start().getLocation())
                && !boundary.includes(edge);
    }
}

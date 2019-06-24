package org.openstreetmap.atlas.geography.atlas.items.complex.roundabout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.walker.SimpleEdgeWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.JunctionTag;

/**
 * A {@link ComplexEntity} representation of a roundabout. To form a valid {@link ComplexRoundabout}
 * the source {@link Edge} and all connected Edges tagged as a roundabout must meet the following
 * criteria: be one way; be car navigable; together form a single closed {@link Route} that has the
 * appropriate directionality based on the country. Adapted from the MalformedRoundaboutCheck in
 * Atlas-Checks.
 *
 * @author bbreithaupt
 * @author savannahostrowski
 */
public class ComplexRoundabout extends ComplexEntity
{
    protected static final String WRONG_WAY_INVALIDATION = "This roundabout is going the wrong direction, or has been improperly tagged as a roundabout.";
    protected static final String INCOMPLETE_ROUTE_INVALIDATION = "This roundabout does not form a single, one-way, complete, car navigable route.";
    private static final List<String> LEFT_DRIVING_COUNTRIES_DEFAULT = Arrays.asList("AIA", "ATG",
            "AUS", "BGD", "BHS", "BMU", "BRB", "BRN", "BTN", "BWA", "CCK", "COK", "CXR", "CYM",
            "CYP", "DMA", "FJI", "FLK", "GBR", "GGY", "GRD", "GUY", "HKG", "IDN", "IMN", "IND",
            "IRL", "JAM", "JEY", "JPN", "KEN", "KIR", "KNA", "LCA", "LKA", "LSO", "MAC", "MDV",
            "MLT", "MOZ", "MSR", "MUS", "MWI", "MYS", "NAM", "NFK", "NIU", "NPL", "NRU", "NZL",
            "PAK", "PCN", "PNG", "SGP", "SGS", "SHN", "SLB", "SUR", "SWZ", "SYC", "TCA", "THA",
            "TKL", "TLS", "TON", "TTO", "TUV", "TZA", "UGA", "VCT", "VGB", "VIR", "WSM", "ZAF",
            "ZMB", "ZWE");

    private final List<String> invalidationReasons = new ArrayList<>();
    private Set<Edge> roundaboutEdgeSet;
    private Route roundaboutRoute;

    /**
     * An enum of RoundaboutDirections
     */
    public enum RoundaboutDirection
    {
        CLOCKWISE,
        COUNTERCLOCKWISE,
        // Handles the case where we were unable to get any information about the roundabout's
        // direction.
        UNKNOWN
    }

    /**
     * This method returns a RoundaboutDirection enum which indicates direction of the flow of
     * traffic based on the cross product of two adjacent edges. This method leverages the
     * right-hand rule as it relates to the directionality of two vectors.
     *
     * @see "https://en.wikipedia.org/wiki/Right-hand_rule"
     * @param roundaboutEdges
     *            A list of Edges in a roundabout
     * @return CLOCKWISE or COUNTERCLOCKWISE if the majority of the edges have positive or negative
     *         cross products respectively, and UNKNOWN if all edge cross products are 0 or if the
     *         roundabout's geometry is malformed
     */
    private static RoundaboutDirection findRoundaboutDirection(final Route roundaboutEdges)
    {
        int clockwiseCount = 0;
        int counterClockwiseCount = 0;

        for (int idx = 0; idx < roundaboutEdges.size(); idx++)
        {
            // Get the Edges to use in the cross product
            final Edge edge1 = roundaboutEdges.get(idx);
            // We mod the roundabout edges here so that we can get the last pair of edges in the
            // Roundabout correctly
            final Edge edge2 = roundaboutEdges.get((idx + 1) % roundaboutEdges.size());
            // Get the cross product and then the direction of the roundabout
            final double crossProduct = getCrossProduct(edge1, edge2);
            final RoundaboutDirection direction;
            if (crossProduct < 0)
            {
                direction = RoundaboutDirection.COUNTERCLOCKWISE;
            }
            else if (crossProduct > 0)
            {
                direction = RoundaboutDirection.CLOCKWISE;
            }
            else
            {
                direction = RoundaboutDirection.UNKNOWN;
            }

            // If the direction is UNKNOWN then we continue to the next iteration because we do not
            // Have any new information about the roundabout's direction
            if (direction.equals(RoundaboutDirection.UNKNOWN))
            {
                continue;
            }
            if (direction.equals(RoundaboutDirection.CLOCKWISE))
            {
                clockwiseCount += 1;
            }
            if (direction.equals(RoundaboutDirection.COUNTERCLOCKWISE))
            {
                counterClockwiseCount += 1;
            }
        }
        // Return the Enum for whatever has the highest count
        if (clockwiseCount > counterClockwiseCount)
        {
            return RoundaboutDirection.CLOCKWISE;
        }
        else if (clockwiseCount < counterClockwiseCount)
        {
            return RoundaboutDirection.COUNTERCLOCKWISE;
        }
        else
        {
            return RoundaboutDirection.UNKNOWN;
        }
    }

    /**
     * This method returns the cross product between two adjacent edges.
     *
     * @see "https://en.wikipedia.org/wiki/Cross_product"
     * @param edge1
     *            An Edge entity in the roundabout
     * @param edge2
     *            An Edge entity in the roundabout adjacent to edge1
     * @return A double corresponding to the cross product between two edges
     */
    private static double getCrossProduct(final Edge edge1, final Edge edge2)
    {
        // Get the nodes' latitudes and longitudes to use in deriving the vectors
        final double node1Y = edge1.start().getLocation().getLatitude().asDegrees();
        final double node1X = edge1.start().getLocation().getLongitude().asDegrees();
        final double node2Y = edge1.end().getLocation().getLatitude().asDegrees();
        final double node2X = edge1.end().getLocation().getLongitude().asDegrees();
        final double node3Y = edge2.end().getLocation().getLatitude().asDegrees();
        final double node3X = edge2.end().getLocation().getLongitude().asDegrees();

        // Get the vectors from node 2 to 1, and node 2 to 3
        final double vector1X = node2X - node1X;
        final double vector1Y = node2Y - node1Y;
        final double vector2X = node2X - node3X;
        final double vector2Y = node2Y - node3Y;

        // The cross product tells us the direction of the orthogonal vector, which is
        // Directly related to the direction of rotation/traffic
        return (vector1X * vector2Y) - (vector1Y * vector2X);
    }

    public ComplexRoundabout(final Edge source)
    {
        this(source, LEFT_DRIVING_COUNTRIES_DEFAULT);
    }

    public ComplexRoundabout(final Edge source, final List leftDrivingCountries)
    {
        super(source);
        try
        {
            if (!(
            // Make sure that the source has an iso_country_code
            source.getTag(ISOCountryTag.KEY).isPresent()
                    // Make sure that the source is an instances of a roundabout
                    && JunctionTag.isRoundabout(source)
                    // Make sure that we are looking at a master edge
                    && (source.isMasterEdge())))
            {
                throw new CoreException("Invalid source Edge ({}) for a roundabout.",
                        source.getIdentifier());
            }
            final String isoCountryCode = source.tag(ISOCountryTag.KEY).toUpperCase();

            // Get all edges in the roundabout
            this.roundaboutEdgeSet = new SimpleEdgeWalker(source, this.isRoundaboutEdge())
                    .collectEdges();

            // Try to build a Route from the edges
            try
            {
                this.roundaboutRoute = Route.fromNonArrangedEdgeSet(this.roundaboutEdgeSet, false);
            }
            // If a Route cannot be formed, invalidate the roundabout.
            catch (final CoreException badRoundabout)
            {
                this.invalidationReasons.add(INCOMPLETE_ROUTE_INVALIDATION);
                throw new CoreException(
                        "Exception thrown building Route for Edges {}: {}", this.roundaboutEdgeSet
                                .stream().map(Edge::getIdentifier).collect(Collectors.toSet()),
                        INCOMPLETE_ROUTE_INVALIDATION);
            }

            // Invalidate the roundabout if any of the edges are not car navigable or master Edges,
            // or the route does not
            // form a closed loop.
            if (this.roundaboutEdgeSet.stream()
                    .anyMatch(roundaboutEdge -> !HighwayTag.isCarNavigableHighway(roundaboutEdge)
                            || !roundaboutEdge.isMasterEdge())
                    || !this.roundaboutRoute.start().inEdges().contains(this.roundaboutRoute.end()))
            {
                this.invalidationReasons.add(INCOMPLETE_ROUTE_INVALIDATION);
            }

            // Get the direction of the roundabout
            final RoundaboutDirection direction = findRoundaboutDirection(this.roundaboutRoute);

            // Determine if the roundabout is in a left or right driving country
            final boolean isLeftDriving = leftDrivingCountries.contains(isoCountryCode);

            // If the roundabout traffic is clockwise in a right-driving country, or
            // If the roundabout traffic is counterclockwise in a left-driving country
            if (direction.equals(RoundaboutDirection.CLOCKWISE) && !isLeftDriving
                    || direction.equals(RoundaboutDirection.COUNTERCLOCKWISE) && isLeftDriving)
            {
                this.invalidationReasons.add(WRONG_WAY_INVALIDATION);
            }

            // If there is an invalidation logged, invalidate the roundabout
            if (!this.invalidationReasons.isEmpty())
            {
                throw new CoreException("Error building ComplexRoundabout for Edge {}: {}",
                        source.getIdentifier(), this.invalidationReasons.get(0));
            }
        }
        catch (final Exception exception)
        {
            setInvalidReason(exception.getMessage(), exception);
        }
    }

    /**
     * Function for {@link SimpleEdgeWalker} that gathers connected edges that are part of a
     * roundabout.
     *
     * @return {@link Function} for {@link SimpleEdgeWalker}
     */
    private Function<Edge, Stream<Edge>> isRoundaboutEdge()
    {
        return edge -> edge.connectedEdges().stream().filter(JunctionTag::isRoundabout);
    }

    public List<String> getInvalidationReasons()
    {
        return this.invalidationReasons;
    }

    public Set<Edge> getRoundaboutEdgeSet()
    {
        return this.roundaboutEdgeSet;
    }

    public Route getRoundaboutRoute()
    {
        return this.roundaboutRoute;
    }

    @Override
    public List<ComplexEntityError> getAllInvalidations()
    {
        final List<ComplexEntityError> returnValue = new ArrayList<>();
        if (!isValid())
        {
            getError().ifPresent(returnValue::add);
        }
        return returnValue;
    }

    @Override
    public String toString()
    {
        return String.format("Roundabout of Edges: %s", this.roundaboutEdgeSet);
    }

    @Override
    public Rectangle bounds()
    {
        return this.roundaboutRoute.bounds();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof ComplexRoundabout)
        {
            return this.roundaboutEdgeSet
                    .equals(((ComplexRoundabout) other).getRoundaboutEdgeSet());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
}

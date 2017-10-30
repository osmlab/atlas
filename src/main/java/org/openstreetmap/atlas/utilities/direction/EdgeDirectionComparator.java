package org.openstreetmap.atlas.utilities.direction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.builder.store.AtlasPrimitiveEdge;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * A set of utilities that work with {@link Edge} direction and {@link Heading}.
 *
 * @author Sid
 */
public final class EdgeDirectionComparator
{
    /**
     * Default {@link Angle} limits are used for {@link Heading} comparison. To keep it simple we
     * keep the limits contiguous.
     */
    public static final Angle DEFAULT_OPPOSITE_DIRECTION_LOWER_LIMIT = Angle.degrees(171);
    public static final Angle DEFAULT_OPPOSITE_DIRECTION_UPPER_LIMIT = Angle.degrees(-171);

    public static final Angle DEFAULT_SAME_DIRECTION_LOWER_LIMIT = Angle.degrees(-9);
    public static final Angle DEFAULT_SAME_DIRECTION_UPPER_LIMIT = Angle.degrees(9);

    /*
     * Limit for the heading difference used for detecting if edges are in opposite direction.
     */
    private final Angle oppositeDirectionLowerLimit;
    private final Angle oppositeDirectionUpperLimit;

    /*
     * Limit for the heading difference used for detecting if edges are in same direction.
     */
    private final Angle sameDirectionLowerLimit;
    private final Angle sameDirectionUpperLimit;

    public EdgeDirectionComparator()
    {
        this(DEFAULT_OPPOSITE_DIRECTION_LOWER_LIMIT, DEFAULT_OPPOSITE_DIRECTION_UPPER_LIMIT,
                DEFAULT_SAME_DIRECTION_LOWER_LIMIT, DEFAULT_SAME_DIRECTION_UPPER_LIMIT);
    }

    public EdgeDirectionComparator(final Angle oppositeDirectionLowerLimit,
            final Angle oppositeDirectionUpperLimit, final Angle sameDirectionLowerLimit,
            final Angle sameDirectionUpperLimit)
    {
        this.oppositeDirectionLowerLimit = oppositeDirectionLowerLimit;
        this.oppositeDirectionUpperLimit = oppositeDirectionUpperLimit;
        this.sameDirectionLowerLimit = sameDirectionLowerLimit;
        this.sameDirectionUpperLimit = sameDirectionUpperLimit;
    }

    public boolean isOppositeDirection(final AtlasPrimitiveEdge incomingEdge,
            final AtlasPrimitiveEdge outgoingEdge, final boolean useOverallHeading)
    {
        if (incomingEdge.isReversedEdge(outgoingEdge))
        {
            return true;
        }
        return isOppositeDirection(incomingEdge.getPolyLine(), outgoingEdge.getPolyLine(),
                useOverallHeading);
    }

    /*
     * Detects if given edges are in opposite direction
     */
    public boolean isOppositeDirection(final Edge incomingEdge, final Edge outgoingEdge,
            final boolean useOverallHeading)
    {
        if (incomingEdge.isReversedEdge(outgoingEdge))
        {
            return true;
        }
        return isOppositeDirection(incomingEdge.asPolyLine(), outgoingEdge.asPolyLine(),
                useOverallHeading);
    }

    public boolean isOppositeDirection(final Heading incomingEdgeHeading,
            final Heading outgoingEdgeHeading)
    {
        final Angle headingDifference = incomingEdgeHeading.subtract(outgoingEdgeHeading);
        return headingDifference.isGreaterThanOrEqualTo(this.oppositeDirectionLowerLimit)
                || headingDifference.isLessThan(this.oppositeDirectionUpperLimit);
    }

    public boolean isOppositeDirection(final PolyLine incomingEdgePolyline,
            final PolyLine outgoingEdgePolyline, final boolean useOverallHeading)
    {
        return useOverallHeading
                ? isOppositeDirectionUsingOverallHeading(incomingEdgePolyline, outgoingEdgePolyline)
                : isOppositeDirectionUsingSegmentHeading(incomingEdgePolyline,
                        outgoingEdgePolyline);
    }

    public boolean isOppositeDirectionUsingOverallHeading(final PolyLine incomingEdgePolyline,
            final PolyLine outgoingEdgePolyline)
    {
        boolean oppositeDirection = false;
        if (Math.min(incomingEdgePolyline.size(), outgoingEdgePolyline.size()) > 1)
        {
            final Optional<Heading> incomingSegmentHeading = incomingEdgePolyline.overallHeading();
            final Optional<Heading> outgoingSegmentHeading = outgoingEdgePolyline.overallHeading();
            if (incomingSegmentHeading.isPresent() && outgoingSegmentHeading.isPresent())
            {
                oppositeDirection = isOppositeDirection(incomingSegmentHeading.get(),
                        outgoingSegmentHeading.get());
            }
        }
        return oppositeDirection;
    }

    /*
     * Detects if given polylines are in opposite direction. Comparing the overall edge Heading
     * might not work in case of long curvy edges. The last segment of incomingEdgePolyline and
     * first segment of outgoingEdgePolyline are used to compute the heading difference. Order of
     * the input polyLine matters in computation of heading.
     */
    public boolean isOppositeDirectionUsingSegmentHeading(final PolyLine incomingEdgePolyline,
            final PolyLine outgoingEdgePolyline)
    {
        boolean oppositeDirection = false;
        if (Math.min(incomingEdgePolyline.size(), outgoingEdgePolyline.size()) > 1)
        {
            final List<Segment> incomingSegments = incomingEdgePolyline.segments();
            final Optional<Heading> incomingSegmentHeading = incomingSegments
                    .get(incomingSegments.size() - 1).heading();
            final Optional<Heading> outgoingSegmentHeading = outgoingEdgePolyline.segments().get(0)
                    .heading();
            if (incomingSegmentHeading.isPresent() && outgoingSegmentHeading.isPresent())
            {
                oppositeDirection = isOppositeDirection(incomingSegmentHeading.get(),
                        outgoingSegmentHeading.get());
            }
        }
        return oppositeDirection;
    }

    /*
     * Detects if edgeA is parallel to any of the given edges
     */
    public boolean isParallel(final Edge edgeA, final Collection<Edge> edges,
            final boolean useOverallHeading)
    {
        return edges.stream().anyMatch(edge -> isParallel(edgeA, edge, useOverallHeading));
    }

    /*
     * Detects if edgeA is parallel to edgeB. Direction doesn't matter
     */
    public boolean isParallel(final Edge edgeA, final Edge edgeB, final boolean useOverallHeading)
    {
        return isSameDirection(edgeA, edgeB, useOverallHeading)
                || isOppositeDirection(edgeA, edgeB, useOverallHeading);
    }

    /*
     * Detects if given Atlas Primitive edges are in same direction
     */
    public boolean isSameDirection(final AtlasPrimitiveEdge edgeA, final AtlasPrimitiveEdge edgeB,
            final boolean useOverallHeading)
    {
        // If they are reversed edges, then they are in opposite directions
        if (!edgeA.isReversedEdge(edgeB))
        {
            return isSameDirection(edgeA.getPolyLine(), edgeB.getPolyLine(), useOverallHeading);
        }
        return false;
    }

    /*
     * Detects if given edges are in same direction
     */
    public boolean isSameDirection(final Edge edgeA, final Edge edgeB,
            final boolean useOverallHeading)
    {
        // If they are reversed edges, then they are in opposite directions
        if (!edgeA.isReversedEdge(edgeB))
        {
            return isSameDirection(edgeA.asPolyLine(), edgeB.asPolyLine(), useOverallHeading);
        }
        return false;
    }

    /*
     * Detects if given headings of polylines are in same direction
     */
    public boolean isSameDirection(final Heading headingA, final Heading headingB)
    {
        final Angle headingDifference = headingB.subtract(headingA);
        final boolean sameDirection = headingDifference
                .isGreaterThanOrEqualTo(this.sameDirectionLowerLimit)
                && headingDifference.isLessThan(this.sameDirectionUpperLimit);
        return sameDirection;
    }

    /**
     * Detects if given {@link PolyLine} are in same direction
     *
     * @param polyLineA
     *            First {@link PolyLine}
     * @param polyLineB
     *            Second {@link PolyLine}
     * @param useOverallHeading
     *            flag to indicate whether to use overall {@link Heading} or segment {@link Heading}
     * @return true if {@link PolyLine}s are in same direction
     */
    public boolean isSameDirection(final PolyLine polyLineA, final PolyLine polyLineB,
            final boolean useOverallHeading)
    {
        return useOverallHeading ? isSameDirectionUsingOverallHeading(polyLineA, polyLineB)
                : isSameDirectionUsingSegmentHeading(polyLineA, polyLineB);
    }

    /*
     * Detects if given polylines are in same direction using overall heading
     */
    public boolean isSameDirectionUsingOverallHeading(final PolyLine polyLineA,
            final PolyLine polyLineB)
    {
        boolean sameDirection = false;
        if (!polyLineA.isPoint() && !polyLineB.isPoint())
        {
            final Optional<Heading> headingA = polyLineA.overallHeading();
            final Optional<Heading> headingB = polyLineB.overallHeading();
            if (headingA.isPresent() && headingB.isPresent())
            {
                sameDirection = isSameDirection(headingA.get(), headingB.get());
            }
        }
        return sameDirection;
    }

    /*
     * Detects if given polylines are in same direction using segment heading
     */
    public boolean isSameDirectionUsingSegmentHeading(final PolyLine incomingEdgePolyline,
            final PolyLine outgoingEdgePolyline)
    {
        boolean sameDirection = false;
        if (!incomingEdgePolyline.isPoint() && !outgoingEdgePolyline.isPoint())
        {
            final Optional<Heading> incomingSegmentHeading = incomingEdgePolyline.segments()
                    .get(incomingEdgePolyline.segments().size() - 1).heading();
            final Optional<Heading> outgoingSegmentHeading = outgoingEdgePolyline.segments().get(0)
                    .heading();
            if (incomingSegmentHeading.isPresent() && outgoingSegmentHeading.isPresent())
            {
                sameDirection = isSameDirection(incomingSegmentHeading.get(),
                        outgoingSegmentHeading.get());
            }
        }
        return sameDirection;
    }
}

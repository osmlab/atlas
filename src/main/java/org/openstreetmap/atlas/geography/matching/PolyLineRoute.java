package org.openstreetmap.atlas.geography.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * A candidate {@link PolyLine} route matching the source {@link PolyLine}.
 * <p>
 * This is a re-constructed {@link PolyLine} from {@link Segment}s coming from a list of candidate
 * {@link PolyLine}s. The algorithm tries to find the best set of connected {@link Segment}s that
 * belong to many other {@link PolyLine}s and that matches the source {@link PolyLine}
 *
 * @author matthieun
 */
public final class PolyLineRoute implements Comparable<PolyLineRoute>
{
    private final List<Segment> candidateSegments;
    private final Distance cost;
    private final PolyLine source;
    private final List<PolyLine> candidates;
    private PolyLine polyLine;

    protected static PolyLineRoute startFrom(final PolyLine source, final List<PolyLine> candidates,
            final int polyLineIndex, final int segmentIndex)
    {
        final List<Segment> candidateSegments = new ArrayList<>();
        candidateSegments.add(candidateSegment(candidates, polyLineIndex, segmentIndex));
        return new PolyLineRoute(source, candidates, candidateSegments);
    }

    private static Segment candidateSegment(final List<PolyLine> candidates,
            final int polyLineIndex, final int segmentIndex)
    {
        return new Segment(candidates.get(polyLineIndex).get(segmentIndex),
                segmentIndex < candidates.get(polyLineIndex).size() - 1
                        ? candidates.get(polyLineIndex).get(segmentIndex + 1)
                        : candidates.get(polyLineIndex).first());
    }

    private PolyLineRoute(final PolyLine source, final List<PolyLine> candidates,
            final List<Segment> candidateSegments)
    {
        this.candidateSegments = candidateSegments;
        this.source = source;
        this.candidates = candidates;
        this.cost = this.source.averageOneWayDistanceTo(this.asPolyLine())
                .add(this.asPolyLine().size() > 2 ? new PolyLine(this.asPolyLine().innerLocations())
                        .averageOneWayDistanceTo(this.source) : Distance.ZERO);
    }

    public PolyLine asPolyLine()
    {
        if (this.polyLine == null)
        {
            final List<Location> locations = new ArrayList<>();
            this.candidateSegments.forEach(segment -> locations.add(segment.first()));
            if (this.source instanceof Polygon)
            {
                return new Polygon(locations);
            }
            else
            {
                locations.add(this.candidateSegments.get(this.candidateSegments.size() - 1).last());
                return new PolyLine(locations);
            }
        }
        return this.polyLine;
    }

    @Override
    public int compareTo(final PolyLineRoute other)
    {
        return this.cost.isGreaterThan(other.getCost()) ? 1
                : this.cost.isLessThan(other.getCost()) ? -1 : 0;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof PolyLineRoute)
        {
            return ((PolyLineRoute) other).asPolyLine().equals(this.asPolyLine());
        }
        return false;
    }

    public Distance getCost()
    {
        return this.cost;
    }

    @Override
    public int hashCode()
    {
        return asPolyLine().hashCode();
    }

    @Override
    public String toString()
    {
        return "[PolyLineRoute: " + asPolyLine() + "]";
    }

    protected Optional<Location> canAppend(final int polyLineIndex, final int segmentIndex)
    {
        // The segment index and the index of the first point of the segment in the polyline are
        // the same!
        final Location end = this.candidateSegments.get(this.candidateSegments.size() - 1).last();
        final Location proposed = this.candidates.get(polyLineIndex).get(segmentIndex);
        final Segment candidateSegment = candidateSegment(this.candidates, polyLineIndex,
                segmentIndex);
        final boolean matchingLocations = end.equals(proposed);
        final boolean candidateSegmentAlreadyContained = this.candidateSegments
                .contains(candidateSegment);
        if (matchingLocations && !candidateSegmentAlreadyContained)
        {
            return Optional.of(proposed);
        }
        else
        {
            return Optional.empty();
        }
    }

    protected PolyLineRoute copyAndAppend(final int polyLineIndex, final int segmentIndex)
    {
        if (canAppend(polyLineIndex, segmentIndex).isPresent())
        {
            final List<Segment> polyLineIndexAndSegmentIndex = new ArrayList<>(
                    this.candidateSegments);
            polyLineIndexAndSegmentIndex
                    .add(candidateSegment(this.candidates, polyLineIndex, segmentIndex));
            return new PolyLineRoute(this.source, this.candidates, polyLineIndexAndSegmentIndex);
        }
        else
        {
            throw new CoreException("Unable to append {} and {}", asPolyLine(),
                    getSegment(polyLineIndex, segmentIndex));
        }
    }

    private Segment getSegment(final int polyLineIndex, final int segmentIndex)
    {
        return this.candidates.get(polyLineIndex).segments().get(segmentIndex);
    }
}

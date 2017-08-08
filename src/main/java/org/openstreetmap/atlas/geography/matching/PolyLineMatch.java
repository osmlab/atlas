package org.openstreetmap.atlas.geography.matching;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.openstreetmap.atlas.utilities.threads.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Best match between a {@link PolyLine} and some other set of {@link PolyLine}s.
 *
 * @author matthieun
 */
public class PolyLineMatch
{
    private static final Logger logger = LoggerFactory.getLogger(PolyLineMatch.class);

    private final PolyLine source;
    private final List<PolyLine> candidates;

    /**
     * Constructor
     *
     * @param source
     *            The source {@link PolyLine} to try to match.
     * @param candidates
     *            The candidate {@link PolyLine}s that will provide the segments for the match
     */
    public PolyLineMatch(final PolyLine source, final List<PolyLine> candidates)
    {
        this.source = source;
        this.candidates = candidates;
    }

    /**
     * Find the best set of {@link Segment}s that form a {@link PolyLineRoute} that matches the
     * source {@link PolyLine} with a costDistance that is less than the threshold provided.
     *
     * @param threshold
     *            The threshold provided
     * @return The best re-constructed route. Empty if none.
     */
    public Optional<PolyLineRoute> match(final Distance threshold)
    {
        if (this.candidates.isEmpty())
        {
            return Optional.empty();
        }
        PolyLineRoute best = null;
        final SortedSet<PolyLineRoute> candidateRoutes = new TreeSet<>();
        final Set<Location> visitedStitchingLocations = new HashSet<>();
        int priorNumberOfCandidateRoutes = -1;

        boolean candidateRoutesEmpty = candidateRoutes.isEmpty();
        boolean bestNonNull = best != null;
        boolean bestCostTooHigh = false;
        boolean candidateRoutesIncreased = false;

        while (candidateRoutesEmpty || bestNonNull && bestCostTooHigh && candidateRoutesIncreased)
        {
            priorNumberOfCandidateRoutes = candidateRoutes.size();
            final Set<PolyLineRoute> toAdd = new HashSet<>();
            for (int polyLineIndex = 0; polyLineIndex < this.candidates.size(); polyLineIndex++)
            {
                final PolyLine candidate = this.candidates.get(polyLineIndex);
                final List<Segment> segments = candidate.segments();
                for (int segmentIndex = 0; segmentIndex < segments.size(); segmentIndex++)
                {
                    if (candidateRoutes.isEmpty())
                    {
                        toAdd.add(PolyLineRoute.startFrom(this.source, this.candidates,
                                polyLineIndex, segmentIndex));
                    }
                    else
                    {
                        for (final PolyLineRoute existing : candidateRoutes)
                        {
                            final Optional<Location> stitchingLocation = existing
                                    .canAppend(polyLineIndex, segmentIndex);
                            if (stitchingLocation.isPresent()
                                    && !visitedStitchingLocations.contains(stitchingLocation.get()))
                            {
                                visitedStitchingLocations.add(stitchingLocation.get());
                                final PolyLineRoute elected = existing.copyAndAppend(polyLineIndex,
                                        segmentIndex);
                                if (!candidateRoutes.contains(elected))
                                {
                                    toAdd.add(elected);
                                }
                            }
                        }
                    }
                }
            }
            candidateRoutes.addAll(toAdd);
            best = candidateRoutes.first();

            candidateRoutesEmpty = candidateRoutes.isEmpty();
            bestNonNull = best != null;
            bestCostTooHigh = best.getCost().isGreaterThan(threshold);
            candidateRoutesIncreased = candidateRoutes.size() > priorNumberOfCandidateRoutes;
        }
        return Optional.ofNullable(best);
    }

    /**
     * Find the best set of {@link Segment}s that form a {@link PolyLineRoute} that matches the
     * source {@link PolyLine} with a costDistance that is less than the threshold provided.
     *
     * @param threshold
     *            The threshold provided
     * @param maximum
     *            The maximum {@link Duration} of the computation.
     * @return The best re-constructed route. Empty if none, or if the computation took longer than
     *         the maximum {@link Duration}
     */
    public Optional<PolyLineRoute> match(final Distance threshold, final Duration maximum)
    {
        try (Pool polyLineMatchPool = new Pool(1, "PolyLine Match"))
        {
            final Result<Optional<PolyLineRoute>> result = polyLineMatchPool
                    .queue(() -> match(threshold));
            return result.get(maximum);
        }
        catch (final TimeoutException e)
        {
            logger.warn("Was not able to compute PolyLineMatch in {} for {}", maximum, this.source);
            return Optional.empty();
        }
    }
}

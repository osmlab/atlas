package org.openstreetmap.atlas.geography.atlas.walker;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * This class provides some constructors for simple {@link EdgeWalker}s. These constructors mirror
 * constructors from the abstract parent class, making them public and instantiable. These walkers
 * get all filtering logic from their parameters, and only return a single {@link java.util.Set} of
 * unsorted {@link Edge}s from the {@link #collectEdges()} method.
 *
 * @author bbreithaupt
 */
public class SimpleEdgeWalker extends EdgeWalker
{
    /**
     * An {@link EdgeWalker} where all the filtering is done by gathering the candidates.
     *
     * @param startingEdge
     *            {@link Edge} used to start walking the graph
     * @param nextCandidates
     *            {@link Function} supplier provides that gathers the next set of candidates
     */
    public SimpleEdgeWalker(final Edge startingEdge,
            final Function<Edge, Stream<Edge>> nextCandidates)
    {
        super(startingEdge, nextCandidates);
    }

    /**
     * An {@link EdgeWalker} where all the filtering is done by gathering the candidates.
     *
     * @param startingEdge
     *            {@link Edge} used to start walking the graph
     * @param candidateFilter
     *            {@link Predicate} filter to restrict candidate {@link Edge}s
     * @param nextCandidates
     *            {@link Function} supplier provides that gathers the next set of candidates
     */
    public SimpleEdgeWalker(final Edge startingEdge, final Predicate<Edge> candidateFilter,
            final Function<Edge, Stream<Edge>> nextCandidates)
    {
        super(startingEdge, candidateFilter, nextCandidates);
    }
}

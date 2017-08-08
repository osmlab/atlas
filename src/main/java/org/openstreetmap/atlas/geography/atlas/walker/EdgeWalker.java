package org.openstreetmap.atlas.geography.atlas.walker;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * A base class for collecting {@link Edge}s by walking the graph. Graph traversal is done by taking
 * steps along the path defined by the nextCandidates {@link Function} taking and {@link Edge} and
 * returning a stream of more candidate {@link Edge}s to traverse. Candidate {@link Edge}s are
 * filtered by the candidateFilter {@link Predicate} before becoming part of the collected path.
 * Order is maintained by either the order Edges are traversed or by the {@link Edge}
 * {@link Comparator} provided. Use the {@link EdgeHandler} interface to keep track of state gleaned
 * while walking the road network.
 *
 * @author brian_l_davis
 */
public abstract class EdgeWalker
{
    /**
     * Implementations of an {@link EdgeHandler} will be called during the collections of
     * {@link Edge}s walked.
     */
    public interface EdgeHandler
    {
        /**
         * Called before {@link EdgeHandler#handleEdge(Edge)} for every {@link Edge} that results in
         * no more candidate {@link Edge}s
         *
         * @param edge
         *            the current {@link Edge}
         */
        default void handleBoundaryEdge(final Edge edge)
        {
            // no-op
        }

        /**
         * Called for every {@link Edge} collected along the path
         *
         * @param edge
         *            the current {@link Edge}
         */
        default void handleEdge(final Edge edge)
        {
            // no-op
        }
    }

    public static final Comparator<Edge> DEFAULT_TRAVERSAL_ORDER = null;
    public static final Predicate<Edge> DEFAULT_CANDIDATE_FILTER = edge -> true;

    public static final EdgeHandler DEFAULT_EDGE_HANDLER = new EdgeHandler()
    {
    };
    private final Edge startingEdge;
    private final Comparator<Edge> edgeOrder;
    private final Predicate<Edge> candidateFilter;
    private final Function<Edge, Stream<Edge>> nextCandidates;

    private final EdgeHandler edgeHandler;

    /**
     * Custom {@link Edge} walker
     *
     * @param startingEdge
     *            {@link Edge} used to start walking the graph
     * @param edgeOrder
     *            {@link Comparator} used to order the resulting collection
     * @param candidateFilter
     *            {@link Predicate} filter to restrict candidate {@link Edge}s
     * @param nextCandidates
     *            {@link Function} supplier provides the next step to walk
     */
    protected EdgeWalker(final Edge startingEdge, final Comparator<Edge> edgeOrder,
            final Predicate<Edge> candidateFilter,
            final Function<Edge, Stream<Edge>> nextCandidates)
    {
        this(startingEdge, edgeOrder, candidateFilter, nextCandidates, DEFAULT_EDGE_HANDLER);
    }

    /**
     * Custom {@link Edge} walker with {@link Edge} handling
     *
     * @param startingEdge
     *            {@link Edge} used to start walking the graph
     * @param edgeOrder
     *            {@link Comparator} used to order the resulting collection
     * @param candidateFilter
     *            {@link Predicate} filter to restrict candidate {@link Edge}s
     * @param nextCandidates
     *            {@link Function} supplier provides the next step to walk
     * @param edgeHandler
     *            {@link EdgeHandler} handler that will be called during walk
     */
    protected EdgeWalker(final Edge startingEdge, final Comparator<Edge> edgeOrder,
            final Predicate<Edge> candidateFilter,
            final Function<Edge, Stream<Edge>> nextCandidates, final EdgeHandler edgeHandler)
    {
        this.startingEdge = startingEdge;
        this.edgeOrder = edgeOrder;
        this.candidateFilter = candidateFilter;
        this.nextCandidates = nextCandidates;
        this.edgeHandler = edgeHandler;
    }

    /**
     * Basic traversal ordered {@link Edge} walker
     *
     * @param startingEdge
     *            {@link Edge} used to start walking the graph
     * @param nextCandidates
     *            {@link Function} supplier provides the next step to walk to walk
     */
    protected EdgeWalker(final Edge startingEdge, final Function<Edge, Stream<Edge>> nextCandidates)
    {
        this(startingEdge, DEFAULT_TRAVERSAL_ORDER, DEFAULT_CANDIDATE_FILTER, nextCandidates,
                DEFAULT_EDGE_HANDLER);
    }

    /**
     * Basic traversal ordered {@link Edge} walker with {@link Edge} handling
     *
     * @param startingEdge
     *            {@link Edge} used to start walking the graph
     * @param nextCandidates
     *            {@link Function} supplier provides the next step to walk
     * @param edgeHandler
     *            {@link EdgeHandler} handler that will be called during walk
     */
    protected EdgeWalker(final Edge startingEdge, final Function<Edge, Stream<Edge>> nextCandidates,
            final EdgeHandler edgeHandler)
    {
        this(startingEdge, DEFAULT_TRAVERSAL_ORDER, DEFAULT_CANDIDATE_FILTER, nextCandidates,
                edgeHandler);
    }

    /**
     * Filtered, traversal ordered {@link Edge} walker
     *
     * @param startingEdge
     *            {@link Edge} used to start walking the graph
     * @param candidateFilter
     *            {@link Predicate} filter to restrict candidate {@link Edge}s
     * @param nextCandidates
     *            {@link Function} supplier provides the next step to walk to walk
     */
    protected EdgeWalker(final Edge startingEdge, final Predicate<Edge> candidateFilter,
            final Function<Edge, Stream<Edge>> nextCandidates)
    {
        this(startingEdge, DEFAULT_TRAVERSAL_ORDER, candidateFilter, nextCandidates,
                DEFAULT_EDGE_HANDLER);
    }

    /**
     * Filtered, traversal ordered {@link Edge} walker with {@link Edge} handling
     *
     * @param startingEdge
     *            {@link Edge} used to start walking the graph
     * @param candidateFilter
     *            {@link Predicate} filter to restrict candidate {@link Edge}s
     * @param nextCandidates
     *            {@link Function} supplier provides the next step to walk to walk
     * @param edgeHandler
     *            {@link EdgeHandler} handler that will be called during walk
     */
    protected EdgeWalker(final Edge startingEdge, final Predicate<Edge> candidateFilter,
            final Function<Edge, Stream<Edge>> nextCandidates, final EdgeHandler edgeHandler)
    {
        this(startingEdge, DEFAULT_TRAVERSAL_ORDER, candidateFilter, nextCandidates, edgeHandler);
    }

    /**
     * Gets an Set ({@link TreeSet}) of {@link Edge}s, optionally ordered by the {@link Edge}
     * {@link Comparator}, that's constructed by iteratively adding Edges returned from the
     * {@link Edge} supplier {@link Function}, and filtered by the {@link Predicate}.
     *
     * @return the set of edges walked
     */
    public Set<Edge> collectEdges()
    {
        final Set<Edge> edges = this.edgeOrder == null ? new LinkedHashSet<>()
                : new TreeSet<>(this.edgeOrder);
        final Predicate<Edge> unvisited = edge -> !edges.contains(edge);
        Set<Edge> nextBoundaryEdges = Collections.singleton(this.startingEdge);
        do
        {
            edges.addAll(nextBoundaryEdges);
            nextBoundaryEdges = nextBoundaryEdges.stream().flatMap(edge ->
            {
                final Set<Edge> candidates = this.nextCandidates.apply(edge)
                        .filter(unvisited.and(this.candidateFilter))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (candidates.isEmpty())
                {
                    this.edgeHandler.handleBoundaryEdge(edge);
                }
                if (edge.equals(this.startingEdge))
                {
                    // Check if the starting Edge is a boundary
                    if (!edge.isConnectedAtStartTo(Iterables.asSet(candidates))
                            || !edge.isConnectedAtEndTo(Iterables.asSet(candidates)))
                    {
                        this.edgeHandler.handleBoundaryEdge(edge);
                    }
                }
                this.edgeHandler.handleEdge(edge);
                return candidates.stream();
            }).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        while (nextBoundaryEdges.size() > 0);
        return edges;
    }
}

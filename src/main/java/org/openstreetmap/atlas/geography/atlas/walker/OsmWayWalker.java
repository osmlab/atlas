package org.openstreetmap.atlas.geography.atlas.walker;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;

/**
 * An {@link EdgeWalker} that builds a Way, represented by a {@link Set} of {@link Edge}s ordered by
 * WaySectionIndex, by walking outward in both directions from the starting {@link Edge}.
 *
 * @author brian_l_davis
 */
public class OsmWayWalker extends EdgeWalker
{
    /**
     * Filters in main Edges with the same OSM Identifier
     *
     * @author brian_l_davis
     */
    private static class MainEdgeByOsmIdentifierFilter implements Predicate<Edge>
    {
        private final long osmIdentifier;

        MainEdgeByOsmIdentifierFilter(final long osmIdentifier)
        {
            this.osmIdentifier = osmIdentifier;
        }

        @Override
        public boolean test(final Edge edge)
        {
            return edge.isMainEdge() && edge.getOsmIdentifier() == this.osmIdentifier;
        }
    }

    /**
     * Compares WaySectionIndex of two Edges
     *
     * @author brian_l_davis
     */
    private static class WaySectionComparator implements Comparator<Edge>
    {
        private final ReverseIdentifierFactory reverseIdentifierFactory = new ReverseIdentifierFactory();

        @Override
        public int compare(final Edge left, final Edge right)
        {
            return Long.compare(
                    this.reverseIdentifierFactory.getWaySectionIndex(left.getIdentifier()),
                    this.reverseIdentifierFactory.getWaySectionIndex(right.getIdentifier()));
        }
    }

    /**
     * Grows the {@link Edge} path by all edges connected to an {@link Edge}
     */
    private static final Function<Edge, Stream<Edge>> CONNECTED_EDGES = edge -> edge
            .connectedEdges().stream();

    /**
     * Constructs an {@link EdgeWalker} that collects all the {@link Edge}s that form a complete Way
     *
     * @param edge
     *            any {@link Edge} section of the Way
     */
    public OsmWayWalker(final Edge edge)
    {
        super(edge, new WaySectionComparator(),
                new MainEdgeByOsmIdentifierFilter(edge.getOsmIdentifier()), CONNECTED_EDGES);
    }

    /**
     * Constructs an {@link EdgeWalker} that collects all the {@link Edge}s that form a complete Way
     *
     * @param edge
     *            any {@link Edge} section of the Way
     * @param edgeHandler
     *            an EdgeHandler to collect statistics
     */
    public OsmWayWalker(final Edge edge, final EdgeHandler edgeHandler)
    {
        super(edge, new WaySectionComparator(),
                new MainEdgeByOsmIdentifierFilter(edge.getOsmIdentifier()), CONNECTED_EDGES,
                edgeHandler);
    }
}

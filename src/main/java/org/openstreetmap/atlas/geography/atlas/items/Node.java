package org.openstreetmap.atlas.geography.atlas.items;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * Navigable Node
 *
 * @author matthieun
 */
public abstract class Node extends LocationItem
{
    private static final long serialVersionUID = 2082593591946379000L;

    protected Node(final Atlas atlas)
    {
        super(atlas);
    }

    /**
     * @return The absolute valence, considering all {@link Edge}s, irrespective of
     *         bi-directionality.
     */
    public long absoluteValence()
    {
        return this.connectedEdges().size();
    }

    public SortedSet<Edge> connectedEdges()
    {
        final SortedSet<Edge> result = new TreeSet<>();
        result.addAll(inEdges());
        result.addAll(outEdges());
        return result;
    }

    @Override
    public ItemType getType()
    {
        return ItemType.NODE;
    }

    /**
     * @return The {@link Edge}s that end at this node
     */
    public abstract SortedSet<Edge> inEdges();

    /**
     * @return The {@link Edge}s that start at this node
     */
    public abstract SortedSet<Edge> outEdges();

    @Override
    public String toDiffViewFriendlyString()
    {
        final String relationsString = this.parentRelationsAsDiffViewFriendlyString();

        return "[Node: id=" + this.getIdentifier() + ", location=" + this.getLocation()
                + ", inEdges=" + connectedEdgesIdentifiers(() -> inEdges()) + ", outEdges="
                + connectedEdgesIdentifiers(() -> outEdges()) + ", relations=(" + relationsString
                + "), " + tagString() + "]";
    }

    @Override
    public String toString()
    {
        return "[Node: id=" + this.getIdentifier() + ", location=" + this.getLocation()
                + ", inEdges=" + connectedEdgesIdentifiers(() -> inEdges()) + ", outEdges="
                + connectedEdgesIdentifiers(() -> outEdges()) + ", " + tagString() + "]";
    }

    /**
     * @return The valence considering only the master {@link Edge}s
     */
    public long valence()
    {
        return this.connectedEdges().stream().filter(Edge::isMasterEdge).count();
    }

    private SortedSet<Long> connectedEdgesIdentifiers(
            final Supplier<SortedSet<Edge>> getConnectedEdges)
    {
        final SortedSet<Long> result = new TreeSet<>();
        getConnectedEdges.get().forEach(edge -> result.add(edge.getIdentifier()));
        return result;
    }
}

package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryNode;

/**
 * Keeps track of all {@link TemporaryNode}s created for an atlas {@link Edge} and the occurrence of
 * it in the underlying polyline. This is used specifically for raw atlas way-sectioning.
 *
 * @author mgostintsev
 */
public class NodeOccurrenceCounter
{
    // Mapping of node to occurrence
    private final Map<TemporaryNode, Integer> nodes = new HashMap<>();

    /**
     * Adds the given {@link TemporaryNode} to the store
     *
     * @param node
     *            The {@link TemporaryNode} to add
     */
    public void addNode(final TemporaryNode node)
    {
        // We only care about tracking node existence when adding nodes. The occurrence counter is
        // incremented every time a node is used as a starting point.
        this.nodes.put(node, 1);
    }

    /**
     * Get the {@link TemporaryNode} at this {@link Location}.
     *
     * @param location
     *            The target {@link Location}
     * @return an {@link Optional} containing the {@link TemporaryNode}
     */
    public Optional<TemporaryNode> getNode(final Location location)
    {
        for (final TemporaryNode node : this.getNodes())
        {
            if (node.getLocation().equals(location))
            {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    /**
     * @return all the stored {@link TemporaryNode}s
     */
    public Set<TemporaryNode> getNodes()
    {
        return this.nodes.keySet();
    }

    /**
     * @param node
     *            The {@link TemporaryNode} whose occurrence we want
     * @return the occurrence for the given {@link TemporaryNode}
     */
    public int getOccurrence(final TemporaryNode node)
    {
        return this.nodes.get(node);
    }

    /**
     * Increments the occurrence for the given {@link TemporaryNode}. Occurrence is incremented
     * every time a node is used as a starting node in an edge.
     *
     * @param node
     *            The {@link TemporaryNode} to increment
     */
    public void incrementOccurrence(final TemporaryNode node)
    {
        if (this.nodes.containsKey(node))
        {
            final int oldOccurence = this.nodes.get(node);
            this.nodes.put(node, oldOccurence + 1);
        }
    }

    /**
     * @return the total number of stored {@link TemporaryNode}s
     */
    public int size()
    {
        return this.nodes.size();
    }
}

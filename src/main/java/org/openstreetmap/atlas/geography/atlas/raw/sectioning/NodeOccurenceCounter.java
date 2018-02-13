package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryNode;

/**
 * TODO
 *
 * @author mgostintsev
 */
public class NodeOccurenceCounter
{
    // Mapping of node to occurence
    private final Map<TemporaryNode, Integer> nodes = new HashMap<>();

    public void addNode(final TemporaryNode node)
    {
        this.nodes.put(node, 1);
    }

    public Optional<TemporaryNode> getNode(final Location location)
    {
        for (final TemporaryNode node : this.nodes.keySet())
        {
            if (node.getLocation().equals(location))
            {
                return Optional.of(node);
            }
        }

        return Optional.empty();
    }

    // TODO check if these are all being used
    public Set<TemporaryNode> getNodes()
    {
        return this.nodes.keySet();
    }

    public int getOccurence(final TemporaryNode node)
    {
        return this.nodes.get(node);
    }

    public void incrementOccurence(final TemporaryNode node)
    {
        if (this.nodes.containsKey(node))
        {
            final int oldOccurence = this.nodes.get(node);
            this.nodes.put(node, oldOccurence + 1);
        }
    }

    public int size()
    {
        return this.nodes.size();
    }
}

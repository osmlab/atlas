package org.openstreetmap.atlas.geography.atlas.pbf.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

/**
 * This {@link SectionCounter} counts all way nodes, and can then be used for intersection inference
 * and way slicing
 *
 * @author tony
 */
public class SectionCounter
{
    private Map<Long, Byte> map = new HashMap<>();

    public void clear()
    {
        this.map = new HashMap<>();
    }

    public boolean contains(final Long identifier)
    {
        return this.map.containsKey(identifier);
    }

    public int countFor(final Long identifier)
    {
        return this.map.get(identifier);
    }

    public void increment(final Long nodeIdentifier)
    {
        Byte count = this.map.get(nodeIdentifier);
        if (count == null)
        {
            count = 0;
        }
        this.map.put(nodeIdentifier, (byte) (count + 1));
    }

    public void increment(final WayNode node)
    {
        final long nodeIdentifier = node.getNodeId();
        increment(nodeIdentifier);
    }

    public void incrementAll(final List<WayNode> nodes)
    {
        nodes.forEach(node -> increment(node));
    }

    public boolean isSection(final Long identifier)
    {
        return contains(identifier) && countFor(identifier) > 1;
    }

    public long sectionCount()
    {
        return this.map.values().stream().filter(count -> count > 1).count();
    }

    public Stream<Long> sections()
    {
        return this.map.keySet().stream().filter(identifier -> isSection(identifier));
    }

    public int size()
    {
        return this.map.size();
    }
}

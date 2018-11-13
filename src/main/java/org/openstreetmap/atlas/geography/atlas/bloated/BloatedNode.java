package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class BloatedNode extends Node
{
    private static final long serialVersionUID = -8229589987121555419L;

    private final long identifier;
    private final Location location;
    private final Map<String, String> tags;

    public BloatedNode(final long identifier, final Location location,
            final Map<String, String> tags)
    {
        super(new BloatedAtlas());
        this.identifier = identifier;
        this.location = location;
        this.tags = tags;
    }

    @Override
    public boolean equals(final Object other) // NOSONAR
    {
        if (this == other)
        {
            return true;
        }
        if (other != null && this.getClass() == other.getClass())
        {
            final BloatedNode that = (BloatedNode) other;
            return this.getIdentifier() == that.getIdentifier()
                    && this.getLocation().equals(that.getLocation())
                    && this.getTags().equals(that.getTags());
        }
        return false;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Relation> relations()
    {
        throw new UnsupportedOperationException();
    }
}

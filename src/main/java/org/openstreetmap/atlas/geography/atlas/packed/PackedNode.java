package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Node} built from a {@link PackedAtlas}
 *
 * @author matthieun
 */
public class PackedNode extends Node
{
    private static final long serialVersionUID = -4505441893548672843L;

    private final long index;

    protected PackedNode(final PackedAtlas atlas, final long index)
    {
        super(atlas);
        this.index = index;
    }

    @Override
    public long getIdentifier()
    {
        return packedAtlas().nodeIdentifier(this.index);
    }

    @Override
    public Location getLocation()
    {
        return packedAtlas().nodeLocation(this.index);
    }

    @Override
    public Map<String, String> getTags()
    {
        return packedAtlas().nodeTags(this.index);
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        return packedAtlas().nodeInEdges(this.index);
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        return packedAtlas().nodeOutEdges(this.index);
    }

    @Override
    public Set<Relation> relations()
    {
        return packedAtlas().nodeRelations(this.index);
    }

    private PackedAtlas packedAtlas()
    {
        return (PackedAtlas) getAtlas();
    }
}

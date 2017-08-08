package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Edge} from a {@link PackedAtlas}
 *
 * @author matthieun
 */
public class PackedEdge extends Edge
{
    private static final long serialVersionUID = -7425733302988626570L;

    private final long index;

    protected PackedEdge(final PackedAtlas atlas, final long index)
    {
        super(atlas);
        this.index = index;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return packedAtlas().edgePolyLine(this.index);
    }

    @Override
    public Node end()
    {
        return packedAtlas().edgeEndNode(this.index);
    }

    @Override
    public long getIdentifier()
    {
        return packedAtlas().edgeIdentifier(this.index);
    }

    @Override
    public Map<String, String> getTags()
    {
        return packedAtlas().edgeTags(this.index);
    }

    @Override
    public Set<Relation> relations()
    {
        return packedAtlas().edgeRelations(this.index);
    }

    @Override
    public Node start()
    {
        return packedAtlas().edgeStartNode(this.index);
    }

    private PackedAtlas packedAtlas()
    {
        return (PackedAtlas) this.getAtlas();
    }
}

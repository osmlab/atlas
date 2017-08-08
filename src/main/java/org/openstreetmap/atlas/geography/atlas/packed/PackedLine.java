package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Edge} from a {@link PackedAtlas}
 *
 * @author matthieun
 */
public class PackedLine extends Line
{
    private static final long serialVersionUID = 3087755941210424968L;

    private final long index;

    protected PackedLine(final PackedAtlas atlas, final long index)
    {
        super(atlas);
        this.index = index;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return packedAtlas().linePolyLine(this.index);
    }

    @Override
    public long getIdentifier()
    {
        return packedAtlas().lineIdentifier(this.index);
    }

    @Override
    public Map<String, String> getTags()
    {
        return packedAtlas().lineTags(this.index);
    }

    @Override
    public Set<Relation> relations()
    {
        return packedAtlas().lineRelations(this.index);
    }

    private PackedAtlas packedAtlas()
    {
        return (PackedAtlas) this.getAtlas();
    }
}

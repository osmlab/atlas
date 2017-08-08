package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Edge} from a {@link PackedAtlas}
 *
 * @author matthieun
 */
public class PackedPoint extends Point
{
    private static final long serialVersionUID = -7143958478767647582L;

    private final long index;

    protected PackedPoint(final PackedAtlas atlas, final long index)
    {
        super(atlas);
        this.index = index;
    }

    @Override
    public long getIdentifier()
    {
        return packedAtlas().pointIdentifier(this.index);
    }

    @Override
    public Location getLocation()
    {
        return packedAtlas().pointLocation(this.index);
    }

    @Override
    public Map<String, String> getTags()
    {
        return packedAtlas().pointTags(this.index);
    }

    @Override
    public Set<Relation> relations()
    {
        return packedAtlas().pointRelations(this.index);
    }

    private PackedAtlas packedAtlas()
    {
        return (PackedAtlas) this.getAtlas();
    }
}

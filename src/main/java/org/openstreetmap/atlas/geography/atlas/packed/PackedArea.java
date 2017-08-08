package org.openstreetmap.atlas.geography.atlas.packed;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Area} from a {@link PackedAtlas}
 *
 * @author matthieun
 */
public class PackedArea extends Area
{
    private static final long serialVersionUID = 4578525310383858728L;

    private final long index;

    protected PackedArea(final PackedAtlas atlas, final long index)
    {
        super(atlas);
        this.index = index;
    }

    @Override
    public Polygon asPolygon()
    {
        return packedAtlas().areaPolygon(this.index);
    }

    @Override
    public long getIdentifier()
    {
        return packedAtlas().areaIdentifier(this.index);
    }

    @Override
    public Map<String, String> getTags()
    {
        return packedAtlas().areaTags(this.index);
    }

    @Override
    public Set<Relation> relations()
    {
        return packedAtlas().areaRelations(this.index);
    }

    private PackedAtlas packedAtlas()
    {
        return (PackedAtlas) this.getAtlas();
    }
}

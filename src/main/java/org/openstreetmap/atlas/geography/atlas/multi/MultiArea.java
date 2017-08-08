package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Area} made from a {@link MultiAtlas}.
 *
 * @author matthieun
 */
public class MultiArea extends Area
{
    private static final long serialVersionUID = 4710025391581335160L;

    // Not index!
    private final long identifier;

    private Area subArea;

    protected MultiArea(final MultiAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public Polygon asPolygon()
    {
        return getSubArea().asPolygon();
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.getSubArea().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        return multiAtlas().multifyRelations(getSubArea());
    }

    private Area getSubArea()
    {
        if (this.subArea == null)
        {
            this.subArea = this.multiAtlas().subArea(this.identifier);
        }
        return this.subArea;
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}

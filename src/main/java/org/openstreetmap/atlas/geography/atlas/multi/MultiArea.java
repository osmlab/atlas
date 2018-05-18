package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import com.google.common.collect.Sets;

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

    private SubAreaList subAreaList;

    protected MultiArea(final MultiAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public Polygon asPolygon()
    {
        return getRepresentativeSubArea().asPolygon();
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    public SubAreaList getSubAreas()
    {
        if (this.subAreaList == null)
        {
            this.subAreaList = multiAtlas().subAreas(this.identifier);
        }
        return this.subAreaList;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.getRepresentativeSubArea().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        Set<Relation> unionOfAllParentRelations = new HashSet<>();
        for (final Area subArea : getSubAreas().getSubAreas())
        {
            final Set<Relation> currentSubAreaParentRelations = multiAtlas()
                    .multifyRelations(subArea);
            unionOfAllParentRelations = Sets.union(unionOfAllParentRelations,
                    currentSubAreaParentRelations);
        }
        return unionOfAllParentRelations;
    }

    private Area getRepresentativeSubArea()
    {
        return getSubAreas().getSubAreas().get(0);
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}

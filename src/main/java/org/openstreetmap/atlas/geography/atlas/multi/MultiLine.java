package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import com.google.common.collect.Sets;

/**
 * {@link Area} made from a {@link MultiAtlas}.
 *
 * @author matthieun
 */
public class MultiLine extends Line
{
    private static final long serialVersionUID = 4833193008195471987L;

    // Not index!
    private final long identifier;

    private SubLineList subLineList;

    protected MultiLine(final MultiAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return getRepresentativeSubLine().asPolyLine();
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    public SubLineList getSubLines()
    {
        if (this.subLineList == null)
        {
            this.subLineList = multiAtlas().subLines(this.identifier);
        }
        return this.subLineList;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.getRepresentativeSubLine().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        Set<Relation> unionOfAllParentRelations = new HashSet<>();
        for (final Line subLine : getSubLines().getSubLines())
        {
            final Set<Relation> currentSubLineParentRelations = multiAtlas()
                    .multifyRelations(subLine);
            unionOfAllParentRelations = Sets.union(unionOfAllParentRelations,
                    currentSubLineParentRelations);
        }
        return unionOfAllParentRelations;
    }

    private Line getRepresentativeSubLine()
    {
        return getSubLines().getSubLines().get(0);
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}

package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

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

    private Line subLine;

    protected MultiLine(final MultiAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return getSubLine().asPolyLine();
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.getSubLine().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        return multiAtlas().multifyRelations(getSubLine());
    }

    private Line getSubLine()
    {
        if (this.subLine == null)
        {
            this.subLine = this.multiAtlas().subLine(this.identifier);
        }
        return this.subLine;
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}

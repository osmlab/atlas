package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * A line that is not navigable
 *
 * @author matthieun
 */
public abstract class Line extends LineItem
{
    private static final long serialVersionUID = 5348604376185677L;

    protected Line(final Atlas atlas)
    {
        super(atlas);
    }

    @Override
    public ItemType getType()
    {
        return ItemType.LINE;
    }

    @Override
    public String toDiffViewFriendlyString()
    {
        final Set<Relation> relations = this.relations();
        final StringList relationIds = new StringList();
        for (final Relation relation : relations)
        {
            relationIds.add(relation.getIdentifier());
        }
        final String relationStrings = relationIds.join(",");

        return "[Line: id=" + this.getIdentifier() + ", polyLine=" + this.asPolyLine()
                + ", relations=(" + relationStrings + "), " + tagString() + "]";
    }

    @Override
    public String toString()
    {
        return "[Line: id=" + this.getIdentifier() + ", polyLine=" + this.asPolyLine() + ", "
                + tagString() + "]";
    }
}

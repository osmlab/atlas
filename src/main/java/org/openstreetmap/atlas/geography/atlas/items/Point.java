package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * A Point that is not navigable.
 *
 * @author matthieun
 */
public abstract class Point extends LocationItem
{
    private static final long serialVersionUID = -7888952319754555424L;

    protected Point(final Atlas atlas)
    {
        super(atlas);
    }

    @Override
    public ItemType getType()
    {
        return ItemType.POINT;
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

        return "[Point: id=" + this.getIdentifier() + ", location=" + this.getLocation()
                + ", relations=(" + relationStrings + "), " + tagString() + "]";
    }

    @Override
    public String toString()
    {
        return "[Point: id=" + this.getIdentifier() + ", location=" + this.getLocation() + ", "
                + tagString() + "]";
    }
}

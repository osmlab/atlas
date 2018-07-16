package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;

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
        final String relationsString = this.parentRelationsAsDiffViewFriendlyString();

        return "[Line: id=" + this.getIdentifier() + ", polyLine=" + this.asPolyLine()
                + ", relations=(" + relationsString + "), " + tagString() + "]";
    }

    @Override
    public String toString()
    {
        return "[Line: id=" + this.getIdentifier() + ", polyLine=" + this.asPolyLine() + ", "
                + tagString() + "]";
    }
}

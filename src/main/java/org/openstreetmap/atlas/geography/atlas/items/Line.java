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
    private static final int IDENTIFIER_SUFFIX_LENGTH = 6;
    private static final int COUNTRY_SLICED_DIVISOR = 1000;

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
    public boolean isCountrySliced()
    {
        // Get the last 6 digits of the identifier
        final String stringIdentifier = String.valueOf(this.getIdentifier());
        final int lastSix = stringIdentifier.length() > IDENTIFIER_SUFFIX_LENGTH ? Integer.parseInt(
                stringIdentifier.substring(stringIdentifier.length() - IDENTIFIER_SUFFIX_LENGTH))
                : 0;
        // If the the last 6 digits are not equal to 0 and are dividable by 1000 then this is
        // country sliced
        return lastSix != 0 && lastSix % COUNTRY_SLICED_DIVISOR == 0;
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

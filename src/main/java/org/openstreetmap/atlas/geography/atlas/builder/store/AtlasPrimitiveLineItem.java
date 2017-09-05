package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.Map;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;

/**
 * A primitive object for {@link LineItem}
 *
 * @author tony
 */
public class AtlasPrimitiveLineItem extends AtlasPrimitiveEntity
{
    private static final long serialVersionUID = 4435750184254868724L;
    private final PolyLine polyLine;

    public AtlasPrimitiveLineItem(final long identifier, final PolyLine polyLine,
            final Map<String, String> tags)
    {
        super(identifier, tags);
        this.polyLine = polyLine;
    }

    @Override
    public Rectangle bounds()
    {
        return this.polyLine.bounds();
    }

    public PolyLine getPolyLine()
    {
        return this.polyLine;
    }

    @Override
    public String toString()
    {
        return "AtlasPrimitiveLineItem [polyLine=" + this.polyLine + ", getIdentifier()="
                + getIdentifier() + ", getTags()=" + getTags() + "]";
    }
}

package org.openstreetmap.atlas.geography.atlas.items;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.Snapper;

/**
 * {@link Snapper.SnappedLocation} on an {@link LineItem}
 *
 * @author chunzc
 */
public class SnappedLineItem extends Snapper.SnappedLocation
{
    private static final long serialVersionUID = 4935931723612324295L;
    private final LineItem lineItem;

    public SnappedLineItem(final Snapper.SnappedLocation snap, final LineItem lineItem)
    {
        super(snap.getOrigin(), snap, lineItem.asPolyLine());
        this.lineItem = lineItem;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object == null || object.getClass() != this.getClass())
        {
            return false;
        }
        final SnappedLineItem other = (SnappedLineItem) object;
        return new EqualsBuilder()
                .append(this.getLineItem().getIdentifier(), other.getLineItem().getIdentifier())
                .append(this.getLineItem().asPolyLine(), other.getLineItem().asPolyLine())
                .append(this.getLineItem().getTags(), other.getLineItem().getTags()).isEquals();
    }

    public LineItem getLineItem()
    {
        return this.lineItem;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.getLineItem().asPolyLine())
                .append(this.getLineItem().getIdentifier()).append(this.getLineItem().getTags())
                .toHashCode();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[SnappedLineItem: LineItem: ");
        builder.append(this.lineItem.getIdentifier());
        builder.append(", Origin: ");
        builder.append(getOrigin());
        builder.append(", Snap: ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}

package org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Node;

/**
 * The {@link TemporaryNode} object, keeps track of the bare minimum information required to create
 * an Atlas {@link Node}. It is meant to be as light-weight as possible, keeping track of only the
 * identifier and location.
 *
 * @author mgostintsev
 */
public class TemporaryNode implements Serializable
{
    private static final long serialVersionUID = 8099256780139327645L;

    private final long identifier;
    private final Location location;

    public TemporaryNode(final long identifier, final Location location)
    {
        this.identifier = identifier;
        this.location = location;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof TemporaryNode)
        {
            final TemporaryNode that = (TemporaryNode) other;
            return this.getIdentifier() == that.getIdentifier()
                    && this.getLocation().equals(that.getLocation());
        }
        return false;
    }

    public long getIdentifier()
    {
        return this.identifier;
    }

    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(getIdentifier()).append(getLocation()).hashCode();
    }

    @Override
    public String toString()
    {
        return "[Temporary Node=" + this.getIdentifier() + ", location=" + this.getLocation() + "]";
    }
}

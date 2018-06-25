package org.openstreetmap.atlas.geography.atlas.multi;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * Class to handle relation member collection in {@link MultiAtlasBorderFixer}.
 *
 * @author matthieun
 * @author mkalender
 */
public final class TemporaryRelationMember
{
    private final long identifier;
    private final String role;
    private final ItemType type;

    public TemporaryRelationMember(final long identifier, final String role, final ItemType type)
    {
        this.identifier = identifier;
        this.role = role;
        this.type = type;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof TemporaryRelationMember)
        {
            final TemporaryRelationMember that = (TemporaryRelationMember) other;
            return this.getIdentifier() == that.getIdentifier() && this.getRole().equals(that.role)
                    && this.getType().equals(that.getType());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(getIdentifier()).append(getRole()).append(getType())
                .hashCode();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(getIdentifier());
        builder.append(", ");
        builder.append(getRole());
        builder.append(", ");
        builder.append(getType());
        builder.append("]");

        return builder.toString();
    }

    protected long getIdentifier()
    {
        return this.identifier;
    }

    protected String getRole()
    {
        return this.role;
    }

    protected ItemType getType()
    {
        return this.type;
    }
}

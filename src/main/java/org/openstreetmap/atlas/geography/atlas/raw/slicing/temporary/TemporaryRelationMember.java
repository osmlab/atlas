package org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * The {@link TemporaryRelationMember} object, keeps track of the bare minimum information required
 * to create a valid {@link Relation} member, namely the entity identifier, role and type of entity.
 *
 * @author mgostintsev
 */
public class TemporaryRelationMember
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

    public long getIdentifier()
    {
        return this.identifier;
    }

    public String getRole()
    {
        return this.role;
    }

    public ItemType getType()
    {
        return this.type;
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
        return "[Temporary Relation Member=" + this.getIdentifier() + ", Role=" + this.getRole()
                + ", Type=" + this.getType() + "]";
    }
}

package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * @author lcram
 */
public class RelationMemberChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;
    private final long identifier;
    private final ItemType type;
    private final String role;

    public RelationMemberChangeDescriptor(final ChangeDescriptorType changeType,
            final long identifier, final ItemType type, final String role)
    {
        this.changeType = changeType;
        this.identifier = identifier;
        this.type = type;
        this.role = role;
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
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
    public String toString()
    {
        return "RELATION_MEMBER(" + this.changeType + ", " + this.type + ", " + this.identifier
                + ", " + this.role + ")";
    }
}

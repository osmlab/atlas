package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 */
public class RelationMemberChangeDescriptor implements ChangeDescriptor
{
    private final ChangeDescriptorType changeType;

    public RelationMemberChangeDescriptor(final ChangeDescriptorType changeType)
    {
        this.changeType = changeType;
    }

    @Override
    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeType;
    }
}

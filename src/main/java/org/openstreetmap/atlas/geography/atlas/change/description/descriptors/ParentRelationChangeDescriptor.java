package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * @author lcram
 */
public class ParentRelationChangeDescriptor extends GenericSetChangeDescriptor<Long>
{
    public ParentRelationChangeDescriptor(final ChangeDescriptorType changeType, final Long element)
    {
        super(changeType, element);
    }

    @Override
    public String toString()
    {
        return "PARENT_RELATION(" + this.getChangeDescriptorType() + ", " + this.getElement() + ")";
    }
}

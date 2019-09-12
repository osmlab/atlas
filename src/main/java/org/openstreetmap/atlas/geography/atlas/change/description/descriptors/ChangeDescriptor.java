package org.openstreetmap.atlas.geography.atlas.change.description.descriptors;

import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescription;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * A basic change unit, which when grouped together form a {@link ChangeDescription}.
 * 
 * @author lcram
 */
public interface ChangeDescriptor
{
    ChangeDescriptorType getChangeDescriptorType();
}

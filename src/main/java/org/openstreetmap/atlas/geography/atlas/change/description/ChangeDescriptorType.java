package org.openstreetmap.atlas.geography.atlas.change.description;

import org.openstreetmap.atlas.geography.atlas.change.ChangeType;

/**
 * Three basic types to characterize the change descriptions. This is similar to the
 * {@link ChangeType} enum, but with additional support for a more granular
 * {@link ChangeDescriptorType#UPDATE} type.
 * 
 * @author lcram
 */
public enum ChangeDescriptorType
{
    ADD,
    UPDATE,
    REMOVE
}

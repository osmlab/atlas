package org.openstreetmap.atlas.utilities.identifiers;

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;

/**
 * Generate unique 64 bit (Java long) identifiers for {@link CompleteEntity}s. The identifiers are
 * generated using a hash of the entity's properties, including its geometry and tags. While the
 * identifiers are advertised as unique, 64 bits may not be enough to prevent collisions when used
 * at world-scale.
 * 
 * @author lcram
 */
public class EntityIdentifierGenerator
{
    public long generate(final CompleteEntity entity)
    {
        // TODO implement
        return 0L;
    }

    /**
     * Given some {@link CompleteEntity}, compute a string made up of the concatenated basic entity
     * properties (i.e. the geometry WKT and the tags).
     * 
     * @param entity
     *            the {@link CompleteEntity} to string-ify
     * @return the property string
     */
    private String getPropertyString(final CompleteEntity entity)
    {
        // TODO implement
        return null;
    }

    /**
     * Given some {@link CompleteEntity}, compute a string made up of concatenated type specific
     * entity properties (e.g. for a {@link CompleteNode} this would be the in/out {@link Edge}
     * identifiers).
     * 
     * @param entity
     *            the {@link CompleteEntity} to string-ify
     * @return the property string
     */
    private String getTypeSpecificPropertyString(final CompleteEntity entity)
    {
        // TODO implement
        return null;
    }
}

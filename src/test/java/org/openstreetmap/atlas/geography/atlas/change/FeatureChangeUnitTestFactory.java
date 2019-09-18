package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

/**
 * A class that exposes the package private {@link FeatureChange} constructor for testing purposes
 * only. This constructor should remain package private since it is not for general use. However,
 * subpackages occasionally have tests that need to access the constructor.
 * 
 * @author lcram
 */
public final class FeatureChangeUnitTestFactory
{
    public static FeatureChange build(final ChangeType type, final AtlasEntity after,
            final AtlasEntity before)
    {
        return new FeatureChange(type, after, before);
    }

    private FeatureChangeUnitTestFactory()
    {
    }
}

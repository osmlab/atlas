package org.openstreetmap.atlas.geography.atlas.items;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link AtlasItemIntersectionTest} test data.
 *
 * @author mgostintsev
 */
public class AtlasItemIntersectionTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "intersectionAtlas.atlas.txt")
    private Atlas intersectionAtlas;

    public Atlas getIntersectionAtlas()
    {
        return this.intersectionAtlas;
    }
}

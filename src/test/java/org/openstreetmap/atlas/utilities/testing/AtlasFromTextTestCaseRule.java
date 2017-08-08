package org.openstreetmap.atlas.utilities.testing;

import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * Test fixture data for AtlasFromTextTestCase
 *
 * @author cstaylor
 */
public class AtlasFromTextTestCaseRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "test.txt")
    private Atlas atlas;

    public Atlas atlas()
    {
        return this.atlas;
    }
}

package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class MultipleChangeAtlasTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "MultipleChangeAtlasTest.atlas.txt")
    // @TestAtlas(loadFromTextResource = "MultipleChangeAtlasTest.osm")
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}

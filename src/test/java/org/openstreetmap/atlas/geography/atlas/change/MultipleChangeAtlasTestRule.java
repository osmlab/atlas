package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class MultipleChangeAtlasTestRule extends CoreTestRule
{
    // MultipleChangeAtlasTest.osm is the osm file from which MultipleChangeAtlasTest.atlas.txt was
    // created.
    // @TestAtlas(loadFromTextResource = "MultipleChangeAtlasTest.osm")
    @TestAtlas(loadFromTextResource = "MultipleChangeAtlasTest.atlas.txt")
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}

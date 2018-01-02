package org.openstreetmap.atlas.utilities.testing;

import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * @author matthieun
 */
public class TestAtlasTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "josmOsmFile.osm")
    private Atlas atlasFromJosmOsmResource;

    public Atlas getAtlasFromJosmOsmResource()
    {
        return this.atlasFromJosmOsmResource;
    }
}

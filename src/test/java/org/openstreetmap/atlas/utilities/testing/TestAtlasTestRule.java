package org.openstreetmap.atlas.utilities.testing;

import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * {@link TestAtlasTest} test data
 *
 * @author matthieun
 */
public class TestAtlasTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "josmOsmFile.osm")
    private Atlas atlasFromJosmOsmResource;

    @TestAtlas(loadFromOsmResource = "osmFile.osm")
    private Atlas atlasFromOsmResource;

    public Atlas getAtlasFromJosmOsmResource()
    {
        return this.atlasFromJosmOsmResource;
    }

    public Atlas getAtlasFromOsmResource()
    {
        return this.atlasFromOsmResource;
    }
}

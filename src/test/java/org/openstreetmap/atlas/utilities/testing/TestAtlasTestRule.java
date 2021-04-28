package org.openstreetmap.atlas.utilities.testing;

import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * {@link TestAtlasTest} test data
 *
 * @author matthieun
 * @author bbreithaupt
 */
public class TestAtlasTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "josmOsmFile.osm")
    private Atlas atlasFromJosmOsmResource;

    @TestAtlas(loadFromOsmResource = "osmFile.osm")
    private Atlas atlasFromOsmResource;

    @TestAtlas(loadFromJosmOsmResource = "josmOsmFile.osm", iso = "USA")
    private Atlas atlasFromJosmOsmResourceISO;

    @TestAtlas(loadFromOsmResource = "osmFile.osm", iso = "USA")
    private Atlas atlasFromOsmResourceISO;

    public Atlas getAtlasFromJosmOsmResource()
    {
        return this.atlasFromJosmOsmResource;
    }

    public Atlas getAtlasFromJosmOsmResourceISO()
    {
        return this.atlasFromJosmOsmResourceISO;
    }

    public Atlas getAtlasFromOsmResource()
    {
        return this.atlasFromOsmResource;
    }

    public Atlas getAtlasFromOsmResourceISO()
    {
        return this.atlasFromOsmResourceISO;
    }
}

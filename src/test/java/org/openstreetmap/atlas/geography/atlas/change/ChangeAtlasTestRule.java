package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class ChangeAtlasTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "ChangeAtlasTest.josm.osm")
    private Atlas atlas;

    @TestAtlas(loadFromJosmOsmResource = "ChangeAtlasTestEdge.josm.osm")
    private Atlas atlasEdge;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getAtlasEdge()
    {
        return this.atlasEdge;
    }
}

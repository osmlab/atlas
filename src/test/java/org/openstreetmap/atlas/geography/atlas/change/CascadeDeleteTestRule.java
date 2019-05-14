package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author Yazad Khambata
 */
public class CascadeDeleteTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "MultipleChangeAtlasTest.osm")
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}

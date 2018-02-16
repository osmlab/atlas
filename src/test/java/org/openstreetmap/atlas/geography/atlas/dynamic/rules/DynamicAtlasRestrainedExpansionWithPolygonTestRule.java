package org.openstreetmap.atlas.geography.atlas.dynamic.rules;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class DynamicAtlasRestrainedExpansionWithPolygonTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "DynamicAtlasRestrainedExpansionWithPolygonTest.osm")
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}

package org.openstreetmap.atlas.geography.atlas.dynamic.rules;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class DynamicAtlasAggressiveRelationsTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "DynamicAtlasAgressiveRelationsTest_11-998-708.josm.osm")
    private Atlas atlasZ11X998Y708;

    @TestAtlas(loadFromJosmOsmResource = "DynamicAtlasAgressiveRelationsTest_11-999-708.josm.osm")
    private Atlas atlasZ11X999Y708;

    public Atlas getAtlasZ11X998Y708()
    {
        return this.atlasZ11X998Y708;
    }

    public Atlas getAtlasZ11X999Y708()
    {
        return this.atlasZ11X999Y708;
    }
}

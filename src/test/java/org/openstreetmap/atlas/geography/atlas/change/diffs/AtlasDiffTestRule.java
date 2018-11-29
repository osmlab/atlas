package org.openstreetmap.atlas.geography.atlas.change.diffs;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author lcram
 */
public class AtlasDiffTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "DiffAtlas1.josm.osm")
    private Atlas atlas1;

    @TestAtlas(loadFromJosmOsmResource = "DiffAtlas2.josm.osm")
    private Atlas atlas2;

    public Atlas getAtlas1()
    {
        return this.atlas1;
    }

    public Atlas getAtlas2()
    {
        return this.atlas2;
    }
}

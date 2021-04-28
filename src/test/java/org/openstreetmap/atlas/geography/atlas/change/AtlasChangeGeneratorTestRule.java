package org.openstreetmap.atlas.geography.atlas.change;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class AtlasChangeGeneratorTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "nodeBoundsExpansionAtlas.josm.osm")
    private Atlas nodeBoundsExpansionAtlas;

    public Atlas getNodeBoundsExpansionAtlas()
    {
        return this.nodeBoundsExpansionAtlas;
    }
}

package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class ChangeAtlasEdgeValidatorTestRule extends CoreTestRule
{
    @TestAtlas(loadFromJosmOsmResource = "ChangeAtlasEdgeValidatorTest.josm.osm")
    private Atlas atlasEdge;

    public Atlas getAtlasEdge()
    {
        return this.atlasEdge;
    }
}

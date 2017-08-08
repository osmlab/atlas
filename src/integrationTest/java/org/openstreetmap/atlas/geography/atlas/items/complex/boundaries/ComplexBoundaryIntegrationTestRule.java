package org.openstreetmap.atlas.geography.atlas.items.complex.boundaries;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author matthieun
 */
public class ComplexBoundaryIntegrationTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "HTI-DOM-Boundaries.atlas.txt.gz")
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}

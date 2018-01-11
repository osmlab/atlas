package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link ComplexHarbourTest} test data.
 *
 * @author mgostintsev
 */
public class ComplexHarborTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "harborAsRelation.atlas.txt")
    private Atlas harborAsRelationAtlas;

    @TestAtlas(loadFromTextResource = "harborAsArea.atlas.txt")
    private Atlas harborAsAreaAtlas;

    public Atlas getHarborAsAreaAtlas()
    {
        return this.harborAsAreaAtlas;
    }

    public Atlas getHarborAsRelationAtlas()
    {
        return this.harborAsRelationAtlas;
    }
}

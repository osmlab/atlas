package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test data for {@link ComplexWaterWayTest}.
 *
 * @author mgostintsev
 */
public class ComplexWaterWayTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "canalAsRelationOfCanals.atlas.txt")
    private Atlas canalAsRelationOfCanalEntitiesAtlas;

    @TestAtlas(loadFromTextResource = "canalAsRelation.atlas.txt")
    private Atlas canalAsRelationOfNonCanalEntitiesAtlas;

    public Atlas getCanalAsRelationOfCanalEntitiesAtlas()
    {
        return this.canalAsRelationOfCanalEntitiesAtlas;
    }

    public Atlas getCanalAsRelatonOfNonCanalEntitiesAtlas()
    {
        return this.canalAsRelationOfNonCanalEntitiesAtlas;
    }
}

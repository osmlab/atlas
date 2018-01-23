package org.openstreetmap.atlas.geography.atlas.pbf;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link OsmPbfComplexRelationTest} test rule.
 *
 * @author mgostintsev
 */
public class OsmPbfComplexRelationTestRule extends CoreTestRule
{
    @TestAtlas(loadFromOsmResource = "partialRelation4451979.osm")
    private Atlas partialRelation4451979;

    public Atlas getPartialRelation4451979Atlas()
    {
        return this.partialRelation4451979;
    }
}

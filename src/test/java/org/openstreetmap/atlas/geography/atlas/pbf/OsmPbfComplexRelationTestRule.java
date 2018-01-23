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
    @TestAtlas(loadFromOsmResource = "relation4451979.osm")
    private Atlas relation4451979;

    public Atlas getAtlasForRelation4451979()
    {
        return this.relation4451979;
    }
}

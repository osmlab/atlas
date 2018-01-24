package org.openstreetmap.atlas.geography.atlas.pbf;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * {@link OsmPbfWayToAtlasEdgeTranslationTest} test rule.
 *
 * @author mgostintsev
 */
public class OsmPbfWayToAtlasEdgeTranslationTestRule extends CoreTestRule
{
    @TestAtlas(loadFromOsmResource = "partialRelation4451979.osm")
    private Atlas partialRelation4451979;

    @TestAtlas(loadFromOsmResource = "ferryRelation5831018.osm")
    private Atlas relation5831018;

    public Atlas getFerryRelation5831018Atlas()
    {
        return this.relation5831018;
    }

    public Atlas getPartialRelation4451979Atlas()
    {
        return this.partialRelation4451979;
    }
}

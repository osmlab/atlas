package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test fixture for the {@link BuildingsContainsOsmIdentifierTestCase}
 *
 * @author cstaylor
 */
public class BuildingsContainsOsmIdentifierTestCaseRule extends CoreTestRule
{
    public static final long CONTAINS_OSM_IDENTIFIER = 250786481;

    public static final long DOES_NOT_CONTAIN_OSM_IDENTIFIER = 250786482;

    @TestAtlas(loadFromTextResource = "building_block_atlas.txt")
    private Atlas atlas;

    public ComplexBuilding buildingWithBlocks()
    {
        return Iterables.first(new ComplexBuildingFinder().find(this.atlas, Finder::ignore)).get();
    }
}

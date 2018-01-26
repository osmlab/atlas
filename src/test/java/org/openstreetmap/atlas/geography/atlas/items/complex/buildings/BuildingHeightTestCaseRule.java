package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test fixture for the {@link BuildingHeightTestCase}
 *
 * @author ajayaswal
 */
public class BuildingHeightTestCaseRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "building_with_no_minheight.txt")
    private Atlas noMinHeightatlas;

    @TestAtlas(loadFromTextResource = "building_with_both_heighttags.txt")
    private Atlas bothHeightsatlas;

    public ComplexBuilding buildingWithBaseAndTopHeights()
    {
        return Iterables
                .first(new ComplexBuildingFinder().find(this.bothHeightsatlas, Finder::ignore))
                .get();
    }

    public ComplexBuilding buildingWithNoMinHeight()
    {
        return Iterables
                .first(new ComplexBuildingFinder().find(this.noMinHeightatlas, Finder::ignore))
                .get();
    }
}

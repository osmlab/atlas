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
    public static final long BUILDING_WITH_ZERO_MIN_HEIGHT_ID = 100000000L;
    public static final long BUILDING_WITH_NEGATIVE_MIN_HEIGHT_ID = 200000000L;
    public static final long BUILDING_WITH_BOTH_HEIGHT_ID = 300000000L;
    public static final long BUILDING_WITH_NO_MIN_HEIGHT_ID = 400000000L;
    public static final long BUILDING_WITH_NONNUMERIC_MIN_HEIGHT_ID = 500000000L;

    @TestAtlas(loadFromTextResource = "building_with_minheights.txt")
    private Atlas atlasWithBuildingMinHeights;

    public ComplexBuilding buildingWithBaseAndTopHeights()
    {
        return Iterables
                .first(Iterables.filter(
                        new ComplexBuildingFinder().find(this.atlasWithBuildingMinHeights,
                                Finder::ignore),
                        building -> building.getIdentifier() == BUILDING_WITH_BOTH_HEIGHT_ID))
                .get();
    }

    public ComplexBuilding buildingWithNegativeMinHeight()
    {
        return Iterables.first(Iterables.filter(
                new ComplexBuildingFinder().find(this.atlasWithBuildingMinHeights, Finder::ignore),
                building -> building.getIdentifier() == BUILDING_WITH_NEGATIVE_MIN_HEIGHT_ID))
                .get();
    }

    public ComplexBuilding buildingWithNoMinHeight()
    {
        return Iterables
                .first(Iterables.filter(
                        new ComplexBuildingFinder().find(this.atlasWithBuildingMinHeights,
                                Finder::ignore),
                        building -> building.getIdentifier() == BUILDING_WITH_NO_MIN_HEIGHT_ID))
                .get();
    }

    public ComplexBuilding buildingWithNonNumericMinHeight()
    {
        return Iterables.first(Iterables.filter(
                new ComplexBuildingFinder().find(this.atlasWithBuildingMinHeights, Finder::ignore),
                building -> building.getIdentifier() == BUILDING_WITH_NONNUMERIC_MIN_HEIGHT_ID))
                .get();
    }

    public ComplexBuilding buildingWithZeroMinHeight()
    {
        return Iterables
                .first(Iterables.filter(
                        new ComplexBuildingFinder().find(this.atlasWithBuildingMinHeights,
                                Finder::ignore),
                        building -> building.getIdentifier() == BUILDING_WITH_ZERO_MIN_HEIGHT_ID))
                .get();
    }
}

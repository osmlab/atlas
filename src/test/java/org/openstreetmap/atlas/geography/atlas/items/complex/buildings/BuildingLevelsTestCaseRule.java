package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Test fixture for the {@link BuildingLevelsTestCase}
 *
 * @author ajayaswal
 */
public class BuildingLevelsTestCaseRule extends CoreTestRule
{
    public static final long BUILDING_WITH_ZERO_LEVELS_ID = 100000000L;
    public static final long BUILDING_WITH_NEGATIVE_LEVELS_ID = 200000000L;
    public static final long BUILDING_WITH_NONNUMERIC_LEVELS_ID = 300000000L;
    public static final long BUILDING_WITH_VALID_LEVELS_ID = 400000000L;
    public static final long BUILDING_WITH_NO_MIN_LEVEL_ID = 500000000L;
    public static final long BUILDING_WITH_NO_LEVELS_ID = 600000000L;

    @TestAtlas(loadFromTextResource = "building_with_levels.txt")
    private Atlas atlasWithBuildingLevels;

    public ComplexBuilding buildingWithNegativeLevels()
    {
        return Iterables
                .first(Iterables.filter(
                        new ComplexBuildingFinder().find(this.atlasWithBuildingLevels,
                                Finder::ignore),
                        building -> building.getIdentifier() == BUILDING_WITH_NEGATIVE_LEVELS_ID))
                .get();
    }

    public ComplexBuilding buildingWithNoLevels()
    {
        return Iterables.first(Iterables.filter(
                new ComplexBuildingFinder().find(this.atlasWithBuildingLevels, Finder::ignore),
                building -> building.getIdentifier() == BUILDING_WITH_NO_LEVELS_ID)).get();
    }

    public ComplexBuilding buildingWithNoMinLevel()
    {
        return Iterables
                .first(Iterables.filter(
                        new ComplexBuildingFinder().find(this.atlasWithBuildingLevels,
                                Finder::ignore),
                        building -> building.getIdentifier() == BUILDING_WITH_NO_MIN_LEVEL_ID))
                .get();
    }

    public ComplexBuilding buildingWithNonNumericLevels()
    {
        return Iterables
                .first(Iterables.filter(
                        new ComplexBuildingFinder().find(this.atlasWithBuildingLevels,
                                Finder::ignore),
                        building -> building.getIdentifier() == BUILDING_WITH_NONNUMERIC_LEVELS_ID))
                .get();
    }

    public ComplexBuilding buildingWithValidLevels()
    {
        return Iterables
                .first(Iterables.filter(
                        new ComplexBuildingFinder().find(this.atlasWithBuildingLevels,
                                Finder::ignore),
                        building -> building.getIdentifier() == BUILDING_WITH_VALID_LEVELS_ID))
                .get();
    }

    public ComplexBuilding buildingWithZeroLevels()
    {
        return Iterables
                .first(Iterables.filter(
                        new ComplexBuildingFinder().find(this.atlasWithBuildingLevels,
                                Finder::ignore),
                        building -> building.getIdentifier() == BUILDING_WITH_ZERO_LEVELS_ID))
                .get();
    }
}

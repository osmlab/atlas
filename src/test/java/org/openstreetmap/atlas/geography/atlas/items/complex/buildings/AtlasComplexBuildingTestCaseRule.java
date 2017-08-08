package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area.Known;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Building;

/**
 * Test fixture data for {@link AtlasComplexBuildingTestCase}
 *
 * @author cstaylor
 */
public class AtlasComplexBuildingTestCaseRule extends CoreTestRule
{
    @TestAtlas(buildings = @Building(id = "1", outer = @Area(known = Known.BUILDING_1), inners = @Area(known = Known.BUILDING_2)))
    private Atlas goodAtlas;

    @TestAtlas(buildings = @Building(id = "1", outer = @Area(known = Known.BUILDING_2), inners = @Area(known = Known.BUILDING_1)))
    private Atlas badAtlas;

    public Stream<ComplexBuilding> badBuildings()
    {
        return StreamSupport.stream(
                new ComplexBuildingFinder().find(this.badAtlas, Finder::ignore).spliterator(),
                false);
    }

    public Stream<ComplexBuilding> goodBuildings()
    {
        return StreamSupport.stream(
                new ComplexBuildingFinder().find(this.goodAtlas, Finder::ignore).spliterator(),
                false);
    }
}

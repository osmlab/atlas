package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case for the {@link BuildingHeightTestCase} method
 *
 * @author ajayaswal
 */
public class BuildingHeightTestCase
{
    @Rule
    public BuildingHeightTestCaseRule setup = new BuildingHeightTestCaseRule();

    @Test
    public void shouldContainBaseHeight()
    {
        final ComplexBuilding building = this.setup.buildingWithBaseAndTopHeights();
        Assert.assertTrue(building.baseHeight().isPresent());
        Assert.assertEquals(193, building.baseHeight().get().asMeters(), 0.01);
    }

    @Test
    public void shouldContainTopHeight()
    {
        final ComplexBuilding building = this.setup.buildingWithBaseAndTopHeights();
        Assert.assertTrue(building.topHeight().isPresent());
    }

    @Test
    public void shouldNotContainBaseHeight()
    {
        final ComplexBuilding building = this.setup.buildingWithNoMinHeight();
        Assert.assertFalse(building.baseHeight().isPresent());
        Assert.assertTrue(building.topHeight().isPresent());
    }
}

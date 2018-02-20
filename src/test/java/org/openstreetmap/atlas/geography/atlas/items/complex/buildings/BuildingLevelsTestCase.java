package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case for the {@link BuildingLevelsTestCase} method
 *
 * @author ajayaswal
 */
public class BuildingLevelsTestCase
{
    @Rule
    public BuildingLevelsTestCaseRule setup = new BuildingLevelsTestCaseRule();

    @Test
    public void shouldContainValidLevels()
    {
        final ComplexBuilding building = this.setup.buildingWithValidLevels();
        Assert.assertTrue(building.levels().isPresent());
        Assert.assertEquals(20, building.levels().get(), 0.01);
        Assert.assertTrue(building.minimumLevel().isPresent());
        Assert.assertEquals(10, building.minimumLevel().get(), 0.01);
    }

    @Test
    public void shouldHandleNonNumericLevels()
    {
        final ComplexBuilding building = this.setup.buildingWithNonNumericLevels();
        Assert.assertFalse(building.levels().isPresent());
        Assert.assertFalse(building.minimumLevel().isPresent());
    }

    @Test
    public void shouldNotContainLevels()
    {
        final ComplexBuilding building = this.setup.buildingWithNoLevels();
        Assert.assertFalse(building.levels().isPresent());
    }

    @Test
    public void shouldNotContainMinLevel()
    {
        final ComplexBuilding building = this.setup.buildingWithNoMinLevel();
        Assert.assertFalse(building.minimumLevel().isPresent());
    }

    @Test
    public void shouldParseNegativeLevels()
    {
        final ComplexBuilding building = this.setup.buildingWithNegativeLevels();
        Assert.assertTrue(building.levels().isPresent());
        Assert.assertEquals(-1, building.levels().get(), 0.01);
        Assert.assertTrue(building.minimumLevel().isPresent());
        Assert.assertEquals(-3, building.minimumLevel().get(), 0.01);
    }

    @Test
    public void shouldParseZeroLevels()
    {
        final ComplexBuilding building = this.setup.buildingWithZeroLevels();
        Assert.assertTrue(building.levels().isPresent());
        Assert.assertEquals(0, building.levels().get(), 0.01);
        Assert.assertTrue(building.minimumLevel().isPresent());
        Assert.assertEquals(0, building.minimumLevel().get(), 0.01);
    }
}

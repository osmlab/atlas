package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case for the {@link ComplexBuilding#containsOSMIdentifier(long)} method
 *
 * @author cstaylor
 */
public class BuildingsContainsOsmIdentifierTestCase
{
    @Rule
    public BuildingsContainsOsmIdentifierTestCaseRule setup = new BuildingsContainsOsmIdentifierTestCaseRule();

    @Test
    public void shouldContain()
    {
        final ComplexBuilding building = this.setup.buildingWithBlocks();
        Assert.assertTrue(building.containsOSMIdentifier(
                BuildingsContainsOsmIdentifierTestCaseRule.CONTAINS_OSM_IDENTIFIER));
    }

    @Test
    public void shouldNotContain()
    {
        final ComplexBuilding building = this.setup.buildingWithBlocks();
        Assert.assertFalse(building.containsOSMIdentifier(
                BuildingsContainsOsmIdentifierTestCaseRule.DOES_NOT_CONTAIN_OSM_IDENTIFIER));
    }
}

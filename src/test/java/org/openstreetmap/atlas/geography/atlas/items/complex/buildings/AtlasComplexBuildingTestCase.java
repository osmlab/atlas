package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case the verifies we've fixed the problem of negative surface area
 *
 * @author cstaylor
 */
public class AtlasComplexBuildingTestCase
{
    @Rule
    public AtlasComplexBuildingTestCaseRule setup = new AtlasComplexBuildingTestCaseRule();

    @Test
    public void bad()
    {
        Assert.assertEquals(0L, this.setup.badBuildings().count());
    }

    @Test
    public void good()
    {
        Assert.assertEquals(1L, this.setup.goodBuildings().count());
    }
}

package org.openstreetmap.atlas.utilities.testing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;

/**
 * Example test case illustrating how to use custom rules
 *
 * @author cstaylor
 */
public class AtlasTestCase
{
    @Rule
    public AtlasTestCaseRule setup = new AtlasTestCaseRule();

    @Test
    public void verify()
    {
        Assert.assertNotNull(this.setup.atlas());
        Assert.assertNotNull(this.setup.atlas2());
        Assert.assertNotNull(this.setup.atlas3());
        final Area area = this.setup.atlas3().area(1L);
        Assert.assertNotNull(area);
        Assert.assertEquals(new Polygon(Location.TEST_1), area.getRawGeometry());
    }
}

package org.openstreetmap.atlas.geography.atlas.multi;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * @author matthieun
 */
public class MultiAtlasBorderFixerIntegrationTest
{
    @Rule
    public final MultiAtlasBorderFixerIntegrationTestRule rule = new MultiAtlasBorderFixerIntegrationTestRule();

    @Test
    public void testLoopRoadWithTail()
    {
        // Inspired from http://www.openstreetmap.org/way/42009944
        final Atlas atlas1 = this.rule.getAtlas1();
        final Atlas atlas2 = this.rule.getAtlas2();
        final Atlas combined = new MultiAtlas(atlas1, atlas2);
        final Edge loop1 = combined.edge(42009944000001L);
        Assert.assertEquals(PolyLine.wkt(
                "LINESTRING (10.5551312 48.3416583, 10.5551027 48.341611, 10.5550183 48.3415143, 10.5549357 48.3414668, 10.5548325 48.3414164)"),
                loop1.asPolyLine());
        final Edge loop2 = combined.edge(42009944000002L);
        Assert.assertEquals(
                PolyLine.wkt("LINESTRING (10.5548325 48.3414164, 10.5548105 48.3415201)"),
                loop2.asPolyLine());
        final Edge loop3 = combined.edge(42009944000003L);
        Assert.assertEquals(
                PolyLine.wkt("LINESTRING (10.5548105 48.3415201, 10.5548015 48.3415686)"),
                loop3.asPolyLine());
        final Edge loop4 = combined.edge(42009944000004L);
        Assert.assertEquals(PolyLine.wkt(
                "LINESTRING (10.5548015 48.3415686, 10.5548925 48.3416166, 10.5550334 48.3416375, 10.5551312 48.3416583)"),
                loop4.asPolyLine());
        final Edge loop5 = combined.edge(42009944000005L);
        Assert.assertEquals(PolyLine.wkt(
                "LINESTRING (10.5551312 48.3416583, 10.5552096 48.3417501, 10.5553105 48.3419094)"),
                loop5.asPolyLine());
    }
}

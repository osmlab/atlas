package org.openstreetmap.atlas.geography.atlas.pbf;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;

/**
 * @author matthieun
 */
public class BridgeConfiguredFilterTest
{
    @Test
    public void testBridge()
    {
        final BridgeConfiguredFilter bridgeConfiguredFilter1 = new BridgeConfiguredFilter("", "",
                get("bridgeConfiguredFilter1.json"));
        final BridgeConfiguredFilter bridgeConfiguredFilter2 = new BridgeConfiguredFilter("",
                "my-filter", get("bridgeConfiguredFilter2.json"));

        final CompleteEdge edge = new CompleteEdge(123L, PolyLine.TEST_POLYLINE,
                Maps.hashMap("highway", "trunk"), 1L, 2L, Sets.hashSet());
        final CompletePoint point = new CompletePoint(123L, Location.COLOSSEUM,
                Maps.hashMap("highway", "traffic_signals"), Sets.hashSet());

        Assert.assertFalse(bridgeConfiguredFilter1.test(edge));
        Assert.assertTrue(bridgeConfiguredFilter1.test(point));
        Assert.assertTrue(bridgeConfiguredFilter2.test(edge));
        Assert.assertFalse(bridgeConfiguredFilter2.test(point));
    }

    private Configuration get(final String name)
    {
        return new StandardConfiguration(new InputStreamResource(
                () -> BridgeConfiguredFilterTest.class.getResourceAsStream(name)));
    }
}

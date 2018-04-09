package org.openstreetmap.atlas.geography.sharding;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.StringResource;

/**
 * Tests the functionality of DynamicTileSharding.
 *
 * @author james-gage
 */
public class DynamicTileShardingTest
{
    /**
     * Tests the method that calculates the counts for all SlippyTiles below a certain zoom level.
     */
    @Test
    public void testCalculateTileCountsForAllZoom()
    {
        final Map<SlippyTile, Long> countsAtZoom2 = new HashMap<SlippyTile, Long>()
        {
            {
                put(new SlippyTile(0, 0, 2), (long) 5);
                put(new SlippyTile(1, 0, 2), (long) 3);
                put(new SlippyTile(2, 0, 2), (long) 7);
                put(new SlippyTile(3, 0, 2), (long) 2);
                put(new SlippyTile(0, 1, 2), (long) 4);
                put(new SlippyTile(1, 1, 2), (long) 1);
                put(new SlippyTile(2, 1, 2), (long) 2);
                put(new SlippyTile(3, 1, 2), (long) 5);
                put(new SlippyTile(0, 2, 2), (long) 6);
                put(new SlippyTile(1, 2, 2), (long) 7);
                put(new SlippyTile(2, 2, 2), (long) 4);
                put(new SlippyTile(3, 2, 2), (long) 3);
                put(new SlippyTile(0, 3, 2), (long) 8);
                put(new SlippyTile(1, 3, 2), (long) 6);
                put(new SlippyTile(2, 3, 2), (long) 5);
                put(new SlippyTile(3, 3, 2), (long) 4);
            }
        };
        // The HashMap represents this SlippyTile zoom level
        // -----------------
        // | 5 | 3 | 7 | 2 |
        // | 4 | 1 | 2 | 5 |
        // | 6 | 7 | 4 | 3 |
        // | 8 | 6 | 5 | 4 |
        // -----------------
        final long total = 72;
        final String root = "0-0-0";
        final StringResource rootString = new StringResource(root);
        final DynamicTileSharding dynamicTileSharding = new DynamicTileSharding(rootString);
        final Map<SlippyTile, Long> allCounts = dynamicTileSharding.calculateTileCountsForAllZoom(1,
                countsAtZoom2);
        final long zoom0 = allCounts.get(new SlippyTile(0, 0, 0));
        Assert.assertEquals("The summed counts and the total", zoom0, total);
    }
}

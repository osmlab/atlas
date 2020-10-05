package org.openstreetmap.atlas.geography.sharding;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
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
            private static final long serialVersionUID = 8166718906410476661L;

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

    /**
     * Diff generated between two different trees
     * <p>
     * < 9-165-184+ < 10-330-368 < 10-330-369 < 10-331-368 < 10-331-369 --- > 9-165-184
     * </p>
     * 1. changed children ordering 2. removed children at deepest level
     */
    @Test
    public void testEquals()
    {
        final DynamicTileSharding shardingTreeOriginal = new DynamicTileSharding(
                new InputStreamResource(() -> DynamicTileShardingTest.class
                        .getResourceAsStream("testDynamicSharding.txt")));
        final DynamicTileSharding shardingTreeOriginalCopy = new DynamicTileSharding(
                new InputStreamResource(() -> DynamicTileShardingTest.class
                        .getResourceAsStream("testDynamicSharding.txt")));
        final DynamicTileSharding missingChildren = new DynamicTileSharding(
                new InputStreamResource(() -> DynamicTileShardingTest.class
                        .getResourceAsStream("testDynamicShardingMissingChildren.txt")));
        final DynamicTileSharding differentChildOrdering = new DynamicTileSharding(
                new InputStreamResource(() -> DynamicTileShardingTest.class
                        .getResourceAsStream("testDynamicShardingChildOrdering.txt")));
        // identity
        Assert.assertEquals(shardingTreeOriginal, shardingTreeOriginal);
        // copy
        Assert.assertEquals(shardingTreeOriginal, shardingTreeOriginalCopy);
        // missing children
        Assert.assertNotEquals(shardingTreeOriginal, missingChildren);
        // child order ignore
        Assert.assertEquals(shardingTreeOriginal, differentChildOrdering);
    }

    @Test
    public void testForName()
    {
        final DynamicTileSharding shardingTreeOriginal = new DynamicTileSharding(
                new InputStreamResource(() -> DynamicTileShardingTest.class
                        .getResourceAsStream("testDynamicSharding.txt")));
        Assert.assertEquals(SlippyTile.forName("8-13-39"),
                shardingTreeOriginal.shardForName("8-13-39"));
    }

    @Test(expected = CoreException.class)
    public void testForNameError()
    {
        final DynamicTileSharding shardingTreeOriginal = new DynamicTileSharding(
                new InputStreamResource(() -> DynamicTileShardingTest.class
                        .getResourceAsStream("testDynamicSharding.txt")));
        shardingTreeOriginal.shardForName("7-6-19");
    }

    @Test
    public void testGetName()
    {
        final String root = "0-0-0";
        final StringResource rootString = new StringResource(root);
        final DynamicTileSharding dynamicTileSharding = new DynamicTileSharding(rootString);
        Assert.assertNotNull(dynamicTileSharding.getName());
        Assert.assertNotEquals("N/A", dynamicTileSharding.getName());
    }
}

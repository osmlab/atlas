package org.openstreetmap.atlas.geography.atlas.statistics;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasTest;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticKey;
import org.openstreetmap.atlas.geography.atlas.statistics.AtlasStatistics.StatisticValue;

/**
 * @author matthieun
 */
public class AtlasStatisticsTest
{
    @Test
    public void testCounting()
    {
        final Atlas atlas = new PackedAtlasTest().getAtlas();
        final Counter counter = new Counter();
        final AtlasStatistics statistics = counter.processAtlas(atlas);
        Assert.assertEquals(7.245,
                statistics.get(new StatisticKey("PRIMARY", "length_named", "true")).getCount(),
                0.01);
    }

    @Test
    public void testCSVCompatability()
    {
        final StatisticKey key = new StatisticKey("", "last_edit_user_name", "kepta\"sds'sds");
        // if there is a " or , or \n character within a field in a CSV, the CSV field is wrapped in
        // double quotes and the interior double quote is escaped
        final String correctlyFormattedCSVKey = ",last_edit_user_name,\"kepta\"\"sds'sds\"";
        Assert.assertEquals(key.toString(), correctlyFormattedCSVKey);
    }

    @Test
    public void testMerge()
    {
        final StatisticKey key = new StatisticKey("RESIDENTIAL", "length_named", "TRUE");
        final AtlasStatistics stat1 = new AtlasStatistics();
        stat1.add(key, new StatisticValue(100.0, 500.0));
        final AtlasStatistics stat2 = new AtlasStatistics();
        stat2.add(key, new StatisticValue(200.0, 300.0));
        final AtlasStatistics merged = AtlasStatistics.merge(stat1, stat2);
        Assert.assertEquals(300.0, merged.get(key).getCount(), 0.001);
        Assert.assertEquals(800.0, merged.get(key).getTotalCount(), 0.001);
    }
}

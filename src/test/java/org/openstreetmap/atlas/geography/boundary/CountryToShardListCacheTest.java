package org.openstreetmap.atlas.geography.boundary;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * @author jamesgage
 */
public class CountryToShardListCacheTest
{
    @Test
    public void testGetShardNamesForCountry()
    {
        final Resource countryToShardList = new InputStreamResource(
                () -> CountryToShardListCacheTest.class
                        .getResourceAsStream("countryToShardList.txt"));
        final CountryToShardListCache countryToShardListCache = CountryToShardListCache
                .create(countryToShardList);
        final List<SlippyTile> dMAShards = countryToShardListCache.getShardsForCountry("DMA").get();
        Assert.assertEquals(
                "[[SlippyTile: zoom = 9, x = 168, y = 233], [SlippyTile: zoom = 9, x = 168, y = 234]]",
                dMAShards.toString());
    }
}

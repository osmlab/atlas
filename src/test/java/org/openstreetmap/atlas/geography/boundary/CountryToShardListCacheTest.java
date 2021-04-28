package org.openstreetmap.atlas.geography.boundary;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * @author james-gage
 */
public class CountryToShardListCacheTest
{
    private final Resource countryToShardList = new InputStreamResource(
            () -> CountryToShardListCacheTest.class.getResourceAsStream("countryToShardList.txt"));

    @Test
    public void testGetShardNamesForCountry()
    {
        final CountryToShardListCache cache = new CountryToShardListCache(this.countryToShardList);
        // test that the right DMA shards are returned
        final List<Shard> dMAShards = cache.getShardsForCountry("DMA");
        Assert.assertEquals(
                "[[SlippyTile: zoom = 9, x = 168, y = 233], [SlippyTile: zoom = 9, x = 169, y = 233], "
                        + "[SlippyTile: zoom = 9, x = 168, y = 234], [SlippyTile: zoom = 10, x = 338, y = 468]]",
                dMAShards.toString());
        // test that asking for an invalid country code doesn't break anything
        final List<Shard> noShards = cache.getShardsForCountry("XXX");
        Assert.assertTrue(noShards.isEmpty());
    }

    @Test
    public void testSaveWhenBuildingFromFile()
    {
        final CountryToShardListCache cache = new CountryToShardListCache(this.countryToShardList);
        // test that you get the same file back that was used to initialize the cache
        final ByteArrayResource output = new ByteArrayResource();
        cache.save(output);
        Assert.assertEquals(this.countryToShardList.all(), output.all());
    }

    @Test
    public void testSaveWhenBuildingFromScratch()
    {
        final StringList countries = new StringList();
        countries.add("DMA");
        final CountryBoundaryMap boundaries = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountryToShardListCacheTest.class
                        .getResourceAsStream("DMA_boundary.txt")));
        final Sharding sharding = Sharding.forString("dynamic@" + CountryToShardListCacheTest.class
                .getResource("tree-6-14-100000.txt.gz").getPath());
        // test building the cache from scratch
        final CountryToShardListCache cache = new CountryToShardListCache(boundaries, countries,
                sharding);
        final ByteArrayResource output = new ByteArrayResource();
        cache.save(output);
        Assert.assertEquals("DMA||9-168-233, 9-169-233, 9-168-234, 10-338-468", output.all());
    }
}

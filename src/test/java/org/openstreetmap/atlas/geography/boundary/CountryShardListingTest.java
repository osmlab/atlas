package org.openstreetmap.atlas.geography.boundary;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.sharding.CountryShard;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;

/**
 * @author matthieun
 */
public class CountryShardListingTest
{
    @Test
    public void testMultiPolygonBoundary()
    {
        final StringList countries = new StringList();
        countries.add("ZAF");
        final CountryBoundaryMap boundaries = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountryShardListingTest.class
                        .getResourceAsStream("ZAF_osm_boundary.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        final Sharding sharding = Sharding.forString("dynamic@"
                + CountryShardListingTest.class.getResource("tree-6-14-100000.txt.gz").getPath());
        final MultiMapWithSet<String, Shard> shardSetPerCountry = CountryShardListing
                .countryToShardList(countries, boundaries, sharding);
        final Resource expectedShardList = new InputStreamResource(
                () -> CountryShardListingTest.class
                        .getResourceAsStream("ZAF_osm_shards_with_14.txt"));
        Assert.assertEquals(Iterables.size(expectedShardList.lines()),
                shardSetPerCountry.get(countries.get(0)).size());
        expectedShardList.lines().forEach(line ->
        {
            final CountryShard expected = CountryShard.forName(line);
            Assert.assertTrue(
                    shardSetPerCountry.get(countries.get(0)).contains(expected.getShard()));
        });
    }

    @Test
    public void testSmallCountry()
    {
        final StringList countries = new StringList();
        countries.add("DMA");
        final CountryBoundaryMap boundaries = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryShardListingTest.class
                        .getResourceAsStream("DMA_as_world_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        final Sharding sharding = Sharding.forString("dynamic@"
                + CountryShardListingTest.class.getResource("tree-6-13-100000.txt.gz").getPath());
        final MultiMapWithSet<String, Shard> shardSetPerCountry = CountryShardListing
                .countryToShardList(countries, boundaries, sharding);
        Assert.assertEquals(4, shardSetPerCountry.get(countries.get(0)).size());
        StringList.split("DMA_9-168-233,DMA_9-169-233,DMA_9-168-234,DMA_10-338-468", ",")
                .forEach(line ->
                {
                    final CountryShard expected = CountryShard.forName(line);
                    Assert.assertTrue(
                            shardSetPerCountry.get(countries.get(0)).contains(expected.getShard()));
                });
    }
}

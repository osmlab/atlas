package org.openstreetmap.atlas.geography.boundary;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * @author matthieun
 */
public class CountryToShardListingTest
{
    @Test
    @Ignore
    public void testMultiPolygonBoundary()
    {
        final CountryToShardListing listing = new CountryToShardListing();
        final StringList countries = new StringList();
        countries.add("ZAF");
        final CountryBoundaryMap boundaries = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(() -> CountryToShardListingTest.class
                        .getResourceAsStream("ZAF_osm_boundary.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        final Sharding sharding = Sharding.forString("dynamic@"
                + CountryToShardListingTest.class.getResource("tree-6-14-100000.txt.gz").getPath());
        final StringResource output = new StringResource();
        listing.intersectShards(countries, boundaries, sharding, output);
        final File shardList = new File(CountryToShardListingTest.class
                .getResource("ZAF_osm_shards_with_14.txt").getPath());
        Assert.assertEquals(shardList.all() + "\n", output.writtenString());
    }

    @Test
    @Ignore
    public void testSmallCountry()
    {
        final CountryToShardListing listing = new CountryToShardListing();
        final StringList countries = new StringList();
        countries.add("DMA");
        final CountryBoundaryMap boundaries = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(CountryToShardListingTest.class
                        .getResourceAsStream("DMA_as_world_osm_boundaries.txt.gz"))
                                .withDecompressor(Decompressor.GZIP));
        final Sharding sharding = Sharding.forString("dynamic@"
                + CountryToShardListingTest.class.getResource("tree-6-13-100000.txt.gz").getPath());
        final StringResource output = new StringResource();
        listing.intersectShards(countries, boundaries, sharding, output);
        Assert.assertEquals("DMA_9-168-233\nDMA_9-169-233\nDMA_9-168-234\nDMA_10-338-468\n",
                output.writtenString());
    }
}

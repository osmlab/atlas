package org.openstreetmap.atlas.geography.sharding.converters;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.sharding.CountryShard;
import org.openstreetmap.atlas.geography.sharding.GeoHashTile;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * @author lcram
 */
public class StringToShardConverterTest
{
    @Test
    public void testConverter()
    {
        Assert.fail("TODO fill in rest");
    }

    @Test
    public void testConverterWithMetadata()
    {
        final Tuple<Shard, Optional<String>> tuple1 = new StringToShardConverter()
                .convertWithMetadata("ABC_1-2-3");
        final Tuple<Shard, Optional<String>> tuple2 = new StringToShardConverter()
                .convertWithMetadata("ABC_bb54tp9");
        final Tuple<Shard, Optional<String>> tuple3 = new StringToShardConverter()
                .convertWithMetadata("ABC_1-2-3_zz/xx/yy");
        final Tuple<Shard, Optional<String>> tuple4 = new StringToShardConverter()
                .convertWithMetadata("1-2-3");
        final Tuple<Shard, Optional<String>> tuple5 = new StringToShardConverter()
                .convertWithMetadata("1-2-3_zz/_moremetadata");

        Assert.assertTrue(tuple1.getFirst() instanceof CountryShard);
        Assert.assertEquals("ABC", ((CountryShard) tuple1.getFirst()).getCountry());
        Assert.assertTrue(((CountryShard) tuple1.getFirst()).getShard() instanceof SlippyTile);
        Assert.assertEquals("[SlippyTile: zoom = 1, x = 2, y = 3]",
                ((CountryShard) tuple1.getFirst()).getShard().toString());

        Assert.assertTrue(tuple2.getFirst() instanceof CountryShard);
        Assert.assertEquals("ABC", ((CountryShard) tuple2.getFirst()).getCountry());
        Assert.assertTrue(((CountryShard) tuple2.getFirst()).getShard() instanceof GeoHashTile);
        Assert.assertEquals("[GeoHashTile: value = bb54tp9]",
                ((CountryShard) tuple2.getFirst()).getShard().toString());

        Assert.fail("TODO fill in rest");
    }
}

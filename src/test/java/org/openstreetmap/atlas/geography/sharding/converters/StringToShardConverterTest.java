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
        final Shard shard1 = new StringToShardConverter().convert("ABC_1-2-3");
        Assert.assertTrue(shard1 instanceof CountryShard);
        Assert.assertEquals("ABC", ((CountryShard) shard1).getCountry());
        Assert.assertTrue(((CountryShard) shard1).getShard() instanceof SlippyTile);
        Assert.assertEquals("[SlippyTile: zoom = 1, x = 2, y = 3]",
                ((CountryShard) shard1).getShard().toString());
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
        final Tuple<Shard, Optional<String>> tuple6 = new StringToShardConverter()
                .convertWithMetadata("ABC_DEF_1-2-3_zz/xx/yy");

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

        Assert.assertTrue(tuple3.getFirst() instanceof CountryShard);
        Assert.assertEquals("ABC", ((CountryShard) tuple3.getFirst()).getCountry());
        Assert.assertTrue(((CountryShard) tuple3.getFirst()).getShard() instanceof SlippyTile);
        Assert.assertEquals("[SlippyTile: zoom = 1, x = 2, y = 3]",
                ((CountryShard) tuple3.getFirst()).getShard().toString());
        Assert.assertTrue(tuple3.getSecond().isPresent());
        Assert.assertEquals("zz/xx/yy", tuple3.getSecond().get());

        Assert.assertTrue(tuple4.getFirst() instanceof SlippyTile);
        Assert.assertEquals("[SlippyTile: zoom = 1, x = 2, y = 3]", tuple4.getFirst().toString());
        Assert.assertFalse(tuple4.getSecond().isPresent());

        Assert.assertTrue(tuple5.getFirst() instanceof SlippyTile);
        Assert.assertEquals("[SlippyTile: zoom = 1, x = 2, y = 3]", tuple5.getFirst().toString());
        Assert.assertTrue(tuple5.getSecond().isPresent());
        Assert.assertEquals("zz/_moremetadata", tuple5.getSecond().get());

        Assert.assertTrue(tuple6.getFirst() instanceof CountryShard);
        Assert.assertEquals("ABC", ((CountryShard) tuple6.getFirst()).getCountry());
        Assert.assertTrue(((CountryShard) tuple6.getFirst()).getShard() instanceof CountryShard);
        Assert.assertEquals("DEF",
                ((CountryShard) ((CountryShard) tuple6.getFirst()).getShard()).getCountry());
        Assert.assertTrue(((CountryShard) ((CountryShard) tuple6.getFirst()).getShard())
                .getShard() instanceof SlippyTile);
        Assert.assertEquals("[SlippyTile: zoom = 1, x = 2, y = 3]",
                ((CountryShard) ((CountryShard) tuple6.getFirst()).getShard()).getShard()
                        .toString());
        Assert.assertTrue(tuple6.getSecond().isPresent());
        Assert.assertEquals("zz/xx/yy", tuple6.getSecond().get());
    }

    @Test
    public void testIfGetNameIsParsable()
    {
        final StringToShardConverter converter = new StringToShardConverter();

        final SlippyTile slippyTile = new SlippyTile(9, 12, 8);
        final GeoHashTile geoHashTile = new GeoHashTile("3ghv");
        final CountryShard countryShard1 = new CountryShard("DMA", slippyTile);
        final CountryShard countryShard2 = new CountryShard("AIA", geoHashTile);

        Assert.assertEquals(slippyTile, converter.convert(slippyTile.getName()));
        Assert.assertEquals(slippyTile.toString(),
                converter.convert(slippyTile.getName()).toString());
        Assert.assertEquals(geoHashTile, converter.convert(geoHashTile.getName()));
        Assert.assertEquals(geoHashTile.toString(),
                converter.convert(geoHashTile.getName()).toString());
        Assert.assertEquals(countryShard1, converter.convert(countryShard1.getName()));
        Assert.assertEquals(countryShard1.toString(),
                converter.convert(countryShard1.getName()).toString());
        Assert.assertEquals(countryShard2, converter.convert(countryShard2.getName()));
        Assert.assertEquals(countryShard2.toString(),
                converter.convert(countryShard2.getName()).toString());
    }
}

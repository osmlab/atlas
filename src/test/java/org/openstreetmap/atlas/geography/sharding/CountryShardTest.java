package org.openstreetmap.atlas.geography.sharding;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class CountryShardTest
{
    @Test
    public void testCountryShard()
    {
        final CountryShard countryShard = CountryShard.forName("ABC_1-2-3");
        final SlippyTile tile = SlippyTile.forName("1-2-3");
        Assert.assertEquals(tile.bounds(), countryShard.bounds());
        Assert.assertEquals("ABC", countryShard.getCountry());

        final CountryShard countryShard2 = new CountryShard("ABC", "4-5-6");
        final SlippyTile tile2 = SlippyTile.forName("4-5-6");
        Assert.assertEquals(tile2.bounds(), countryShard2.bounds());
        Assert.assertEquals("ABC", countryShard2.getCountry());
    }
}

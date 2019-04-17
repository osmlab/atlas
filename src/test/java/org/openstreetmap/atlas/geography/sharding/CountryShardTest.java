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
    }
}

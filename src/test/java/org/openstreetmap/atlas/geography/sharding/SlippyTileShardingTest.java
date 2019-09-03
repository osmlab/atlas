package org.openstreetmap.atlas.geography.sharding;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public class SlippyTileShardingTest
{
    @Test
    public void testForName()
    {
        final Sharding sharding = Sharding.forString("slippy@11");
        Assert.assertEquals(SlippyTile.forName("11-998-709"), sharding.shardForName("11-998-709"));
    }

    @Test(expected = CoreException.class)
    public void testForNameError()
    {
        final Sharding sharding = Sharding.forString("slippy@11");
        sharding.shardForName("10-498-354");
    }
}

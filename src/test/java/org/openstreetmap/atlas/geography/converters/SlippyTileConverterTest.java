package org.openstreetmap.atlas.geography.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.geography.sharding.converters.SlippyTileConverter;

/**
 * @author tony
 */
public class SlippyTileConverterTest
{
    private static SlippyTileConverter converter = new SlippyTileConverter();

    @Test
    public void testConversion()
    {
        Assert.assertEquals("0-0-0", converter.convert(SlippyTile.ROOT));
        Assert.assertEquals(SlippyTile.ROOT,
                converter.backwardConvert(converter.convert(SlippyTile.ROOT)));
    }
}

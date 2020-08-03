package org.openstreetmap.atlas.utilities.conversion;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class HexStringByteArrayConverterTest
{
    private static final HexStringByteArrayConverter HEX_STRING_BYTE_ARRAY_CONVERTER = new HexStringByteArrayConverter();

    @Test
    public void testConversion()
    {
        final String hex = "0103000000020000000600000036A094FF7FBA";
        final byte[] bytes2 = HEX_STRING_BYTE_ARRAY_CONVERTER.convert(hex);
        final String hex2 = HEX_STRING_BYTE_ARRAY_CONVERTER.backwardConvert(bytes2);
        Assert.assertEquals(hex, hex2);
    }
}

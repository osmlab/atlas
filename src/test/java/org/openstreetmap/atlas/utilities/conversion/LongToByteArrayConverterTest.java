package org.openstreetmap.atlas.utilities.conversion;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class LongToByteArrayConverterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(LongToByteArrayConverterTest.class);

    private static final LongToByteArrayConverter CONVERTER = new LongToByteArrayConverter();

    @Test
    public void testByteArrayToLong()
    {
        final byte[] bytes = { 0, 0, 0, 0, 0, 0, 0, 3 };
        final long value = 3;

        final long convertedValue = CONVERTER.backwardConvert(bytes);
        Assert.assertEquals(value, convertedValue);
    }

    @Test
    public void testLongToByteArray()
    {
        final byte[] bytes = { 0, 0, 0, 0, 0, 0, 0, 3 };
        final long value = 3;

        final byte[] convertedValue = CONVERTER.convert(value);
        Assert.assertArrayEquals(bytes, convertedValue);
    }
}

package org.openstreetmap.atlas.utilities.conversion;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.arrays.LongArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class LongArrayToListOfByteArrayConverterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(LongArrayToListOfByteArrayConverterTest.class);

    private static final LongArrayToListOfByteArrayConverter CONVERTER = new LongArrayToListOfByteArrayConverter();

    @Test
    public void testListOfByteArrayToLongArray()
    {
        final byte[] bytes1 = { 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 5 };
        LongArray longs = new LongArray(2);
        longs.add(3L);
        longs.add(5L);

        List<byte[]> listOfByteArray = new ArrayList<>();
        listOfByteArray.add(bytes1);

        LongArray convertedFrom = CONVERTER.backwardConvert(listOfByteArray);
        Assert.assertEquals(longs, convertedFrom);

        final byte[] bytes2 = { 0, 0, 0, 0, 0, 0, 0, 3 };
        final byte[] bytes3 = { 0, 0, 0, 0, 0, 0, 0, 5 };
        longs = new LongArray(2);
        longs.add(3L);
        longs.add(5L);

        listOfByteArray = new ArrayList<>();
        listOfByteArray.add(bytes2);
        listOfByteArray.add(bytes3);

        convertedFrom = CONVERTER.backwardConvert(listOfByteArray);
        Assert.assertEquals(longs, convertedFrom);
    }

    @Test
    public void testLongArrayToListOfByteArray()
    {
        final LongArray longs = new LongArray(2);
        longs.add(2L);
        longs.add(6L);

        final byte[] bytes1 = { 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 6 };
        final List<byte[]> listOfByteArray = new ArrayList<>();
        listOfByteArray.add(bytes1);

        final List<byte[]> convertedFrom = CONVERTER.convert(longs);

        for (int i = 0; i < convertedFrom.size(); i++)
        {
            Assert.assertArrayEquals(listOfByteArray.get(i), convertedFrom.get(i));
        }
    }
}

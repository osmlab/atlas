package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.arrays.ByteArrayOfArrays;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoByteArrayOfArraysAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoByteArrayOfArraysAdapterTest.class);
    private static final int TEST_ARRAY_SIZE = 200;
    private static final int TEST_SUBARRAY_SIZE = 10_000;
    private static final String TEST_NAME = "testarray";
    private final ProtoByteArrayOfArraysAdapter adapter = new ProtoByteArrayOfArraysAdapter();

    @Test
    public void testConsistency()
    {
        final ByteArrayOfArrays byteArrayOfArrays = new ByteArrayOfArrays(TEST_ARRAY_SIZE);
        for (int index = 0; index < TEST_ARRAY_SIZE; index++)
        {
            final byte[] items = new byte[TEST_SUBARRAY_SIZE];
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                items[subIndex] = 1;
            }
            byteArrayOfArrays.add(items);
        }
        byteArrayOfArrays.setName(TEST_NAME);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(byteArrayOfArrays);
        logger.info("Took {} to serialize ByteArrayOfArrays", startTime.elapsedSince());

        startTime = Time.now();
        final ByteArrayOfArrays parsedFrom = (ByteArrayOfArrays) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize ByteArrayOfArrays from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(byteArrayOfArrays.getName(), parsedFrom.getName());
        Assert.assertEquals(byteArrayOfArrays.size(), parsedFrom.size());
        for (int index = 0; index < TEST_ARRAY_SIZE; index++)
        {
            final byte[] items1 = byteArrayOfArrays.get(index);
            final byte[] items2 = parsedFrom.get(index);
            Assert.assertEquals(items1.length, items2.length);
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                Assert.assertEquals(items1[subIndex], items2[subIndex]);
            }
        }
    }
}

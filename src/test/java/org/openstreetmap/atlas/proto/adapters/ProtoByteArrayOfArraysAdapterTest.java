package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
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
    private static final int TEST_ARRAY_SIZE = 2000;
    private static final int TEST_SUBARRAY_SIZE = 10_000;
    private static final String TEST_NAME = "testarray";
    private final ProtoByteArrayOfArraysAdapter adapter = new ProtoByteArrayOfArraysAdapter();

    @Test
    public void testConsistency()
    {
        final ByteArrayOfArrays byteArrayOfArrays = new ByteArrayOfArrays(TEST_ARRAY_SIZE,
                TEST_SUBARRAY_SIZE, TEST_SUBARRAY_SIZE);
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
        Assert.assertEquals(byteArrayOfArrays, parsedFrom);
    }

    @Test(expected = CoreException.class)
    public void testNullFields()
    {
        final ByteArrayOfArrays byteArrayOfArrays = new ByteArrayOfArrays(10);

        Assert.assertTrue(byteArrayOfArrays.getName() == null);

        Time startTime = Time.now();
        byte[] contents = this.adapter.serialize(byteArrayOfArrays);
        logger.info("Took {} to serialize ByteArrayOfArrays", startTime.elapsedSince());

        startTime = Time.now();
        final ByteArrayOfArrays parsedFrom = (ByteArrayOfArrays) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize ByteArrayOfArrays from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(byteArrayOfArrays, parsedFrom);

        logger.info("Testing proper handling of null elements...");
        byteArrayOfArrays.add(new byte[10]);
        byteArrayOfArrays.add(null);
        contents = this.adapter.serialize(byteArrayOfArrays);
    }
}

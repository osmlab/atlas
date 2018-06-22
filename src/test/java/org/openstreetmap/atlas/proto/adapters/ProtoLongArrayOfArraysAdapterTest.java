package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.arrays.LongArrayOfArrays;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoLongArrayOfArraysAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoLongArrayOfArraysAdapterTest.class);
    private static final int TEST_ARRAY_SIZE = 200;
    private static final int TEST_SUBARRAY_SIZE = 10_000;
    private static final String TEST_NAME = "testarray";
    private final ProtoLongArrayOfArraysAdapter adapter = new ProtoLongArrayOfArraysAdapter();

    @Test
    public void testConsistency()
    {
        final LongArrayOfArrays longArrayOfArrays = new LongArrayOfArrays(TEST_ARRAY_SIZE);
        for (int index = 0; index < TEST_ARRAY_SIZE; index++)
        {
            final long[] items = new long[TEST_SUBARRAY_SIZE];
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                items[subIndex] = subIndex;
            }
            longArrayOfArrays.add(items);
        }
        longArrayOfArrays.setName(TEST_NAME);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longArrayOfArrays);
        logger.info("Took {} to serialize LongArrayOfArrays", startTime.elapsedSince());

        startTime = Time.now();
        final LongArrayOfArrays parsedFrom = (LongArrayOfArrays) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongArrayOfArrays from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longArrayOfArrays, parsedFrom);
    }

    @Test(expected = CoreException.class)
    public void testNullFields()
    {
        final LongArrayOfArrays longArrayOfArrays = new LongArrayOfArrays(10);

        Assert.assertTrue(longArrayOfArrays.getName() == null);

        Time startTime = Time.now();
        byte[] contents = this.adapter.serialize(longArrayOfArrays);
        logger.info("Took {} to serialize LongArrayOfArrays", startTime.elapsedSince());

        startTime = Time.now();
        final LongArrayOfArrays parsedFrom = (LongArrayOfArrays) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongArrayOfArrays from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longArrayOfArrays, parsedFrom);

        logger.info("Testing proper handling of null elements...");
        longArrayOfArrays.add(new long[10]);
        longArrayOfArrays.add(null);
        contents = this.adapter.serialize(longArrayOfArrays);
    }
}

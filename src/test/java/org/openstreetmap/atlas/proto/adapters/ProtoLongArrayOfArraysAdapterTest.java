package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
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
    private static final int TEST_SUBARRAY_SIZE = 100_000;
    private final ProtoLongArrayOfArraysAdapter adapter = new ProtoLongArrayOfArraysAdapter();

    @Test
    public void testConsistency()
    {
        final LongArrayOfArrays longArrayOfArrays = new LongArrayOfArrays(TEST_ARRAY_SIZE);
        for (long index = 0; index < TEST_ARRAY_SIZE; index++)
        {
            final long[] items = new long[TEST_SUBARRAY_SIZE];
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                items[subIndex] = subIndex;
            }
            longArrayOfArrays.add(items);
        }

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longArrayOfArrays);
        logger.info("Took {} to serialize LongArrayOfArrays", startTime.elapsedSince());

        startTime = Time.now();
        final LongArrayOfArrays parsedFrom = (LongArrayOfArrays) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongArrayOfArrays from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longArrayOfArrays.size(), parsedFrom.size());
        for (int index = 0; index < TEST_ARRAY_SIZE; index++)
        {
            final long[] items1 = longArrayOfArrays.get(index);
            final long[] items2 = parsedFrom.get(index);
            Assert.assertEquals(items1.length, items2.length);
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                Assert.assertEquals(items1[subIndex], items2[subIndex]);
            }
        }
    }
}

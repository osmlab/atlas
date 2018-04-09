package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.arrays.IntegerArrayOfArrays;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoIntegerArrayOfArraysAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoIntegerArrayOfArraysAdapterTest.class);
    private static final int TEST_ARRAY_SIZE = 200;
    private static final int TEST_SUBARRAY_SIZE = 10_000;
    private static final String TEST_NAME = "testarray";
    private final ProtoIntegerArrayOfArraysAdapter adapter = new ProtoIntegerArrayOfArraysAdapter();

    @Test
    public void testConsistency()
    {
        final IntegerArrayOfArrays integerArrayOfArrays = new IntegerArrayOfArrays(TEST_ARRAY_SIZE);
        for (int index = 0; index < TEST_ARRAY_SIZE; index++)
        {
            final int[] items = new int[TEST_SUBARRAY_SIZE];
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                items[subIndex] = subIndex;
            }
            integerArrayOfArrays.add(items);
        }
        integerArrayOfArrays.setName(TEST_NAME);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(integerArrayOfArrays);
        logger.info("Took {} to serialize IntegerArrayOfArrays", startTime.elapsedSince());

        startTime = Time.now();
        final IntegerArrayOfArrays parsedFrom = (IntegerArrayOfArrays) this.adapter
                .deserialize(contents);
        logger.info("Took {} to deserialize IntegerArrayOfArrays from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(integerArrayOfArrays.getName(), parsedFrom.getName());
        Assert.assertEquals(integerArrayOfArrays.size(), parsedFrom.size());
        for (int index = 0; index < TEST_ARRAY_SIZE; index++)
        {
            final int[] items1 = integerArrayOfArrays.get(index);
            final int[] items2 = parsedFrom.get(index);
            Assert.assertEquals(items1.length, items2.length);
            for (int subIndex = 0; subIndex < TEST_SUBARRAY_SIZE; subIndex++)
            {
                Assert.assertEquals(items1[subIndex], items2[subIndex]);
            }
        }
    }
}

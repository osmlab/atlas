package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
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
        final IntegerArrayOfArrays integerArrayOfArrays = new IntegerArrayOfArrays(TEST_ARRAY_SIZE,
                TEST_SUBARRAY_SIZE, TEST_SUBARRAY_SIZE);
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
        Assert.assertEquals(integerArrayOfArrays, parsedFrom);
    }

    @Test(expected = CoreException.class)
    public void testNullFields()
    {
        final IntegerArrayOfArrays integerArrayOfArrays = new IntegerArrayOfArrays(10);

        Assert.assertTrue(integerArrayOfArrays.getName() == null);

        Time startTime = Time.now();
        byte[] contents = this.adapter.serialize(integerArrayOfArrays);
        logger.info("Took {} to serialize IntegerArrayOfArrays", startTime.elapsedSince());

        startTime = Time.now();
        final IntegerArrayOfArrays parsedFrom = (IntegerArrayOfArrays) this.adapter
                .deserialize(contents);
        logger.info("Took {} to deserialize IntegerArrayOfArrays from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(integerArrayOfArrays, parsedFrom);

        logger.info("Testing proper handling of null elements...");
        integerArrayOfArrays.add(new int[10]);
        integerArrayOfArrays.add(null);
        contents = this.adapter.serialize(integerArrayOfArrays);
    }
}

package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.arrays.LongArray;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoLongArrayAdapterTest
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoLongArrayAdapterTest.class);
    private static final int TEST_SIZE = 500_000;
    private static final String TEST_NAME = "testarray";
    private final ProtoLongArrayAdapter adapter = new ProtoLongArrayAdapter();

    @Test
    public void testConsistency()
    {
        final LongArray longArray = new LongArray(TEST_SIZE, TEST_SIZE, TEST_SIZE);
        for (int index = 0; index < TEST_SIZE; index++)
        {
            final long value = index;
            longArray.add(value);
        }
        longArray.setName(TEST_NAME);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longArray);
        logger.info("Took {} to serialize LongArray", startTime.elapsedSince());

        startTime = Time.now();
        final LongArray parsedFrom = (LongArray) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongArray from bytestream", startTime.elapsedSince());

        logger.info("Testing array equality...");
        Assert.assertEquals(longArray, parsedFrom);
    }

    @Test
    public void testNullFields()
    {
        final LongArray longArray = new LongArray(10);

        Assert.assertTrue(longArray.getName() == null);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longArray);
        logger.info("Took {} to serialize LongArray", startTime.elapsedSince());

        startTime = Time.now();
        final LongArray parsedFrom = (LongArray) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongArray from bytestream", startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longArray, parsedFrom);
    }
}

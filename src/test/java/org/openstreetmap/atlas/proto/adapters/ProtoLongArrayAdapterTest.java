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
    private static final int TEST_SIZE = 5_000_000;
    private final ProtoLongArrayAdapter adapter = new ProtoLongArrayAdapter();

    @Test
    public void testConsistency()
    {
        final LongArray longArray = new LongArray(TEST_SIZE);
        for (int index = 0; index < TEST_SIZE; index++)
        {
            final long value = index;
            longArray.add(value);
        }
        longArray.setName("test");

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longArray);
        logger.info("Took {} to serialize LongArray", startTime.elapsedSince());

        startTime = Time.now();
        final LongArray parsedFrom = (LongArray) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongArray from bytestream", startTime.elapsedSince());

        // We have to compare arrays pair-wise since assertArrayEquals tries to print the entire
        // array for some reason
        logger.info("Testing array equality...");
        for (int index = 0; index < TEST_SIZE; index++)
        {
            Assert.assertEquals(longArray.get(index), parsedFrom.get(index));
        }
    }
}

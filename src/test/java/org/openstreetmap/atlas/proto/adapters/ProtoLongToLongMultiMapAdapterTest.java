package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.maps.LongToLongMultiMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoLongToLongMultiMapAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoLongToLongMultiMapAdapterTest.class);
    private static final int TEST_SIZE = 1000;
    private static final int TEST_SUBARRAY_SIZE = 1000;
    private static final String TEST_NAME = "testmap";
    private final ProtoLongToLongMultiMapAdapter adapter = new ProtoLongToLongMultiMapAdapter();

    @Test
    public void testConsistency()
    {
        final LongToLongMultiMap longMultiMap = new LongToLongMultiMap(TEST_NAME, TEST_SIZE);
        for (int index = 0; index < TEST_SIZE; index++)
        {
            final long key = index;
            final long[] values = new long[TEST_SUBARRAY_SIZE];
            for (int subIndex = 0; subIndex < values.length; subIndex++)
            {
                values[subIndex] = subIndex;
            }
            longMultiMap.put(key, values);
        }

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longMultiMap);
        logger.info("Took {} to serialize LongToLongMultiMap", startTime.elapsedSince());

        startTime = Time.now();
        final LongToLongMultiMap parsedFrom = (LongToLongMultiMap) this.adapter
                .deserialize(contents);
        logger.info("Took {} to deserialize LongToLongMultiMap from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longMultiMap, parsedFrom);
    }

    @Test(expected = CoreException.class)
    public void testNullElements()
    {
        final LongToLongMultiMap longMultiMap = new LongToLongMultiMap(TEST_SIZE);

        Time startTime = Time.now();
        byte[] contents = this.adapter.serialize(longMultiMap);
        logger.info("Took {} to serialize LongToLongMultiMap", startTime.elapsedSince());

        startTime = Time.now();
        final LongToLongMultiMap parsedFrom = (LongToLongMultiMap) this.adapter
                .deserialize(contents);
        logger.info("Took {} to deserialize LongToLongMultiMap from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longMultiMap, parsedFrom);

        logger.info("Testing proper handling of null elements...");
        longMultiMap.put(3L, new long[10]);
        longMultiMap.put(4L, null);
        contents = this.adapter.serialize(longMultiMap);
    }
}

package org.openstreetmap.atlas.proto.adapters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.maps.LongToLongMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoLongToLongMapAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoLongToLongMapAdapterTest.class);
    private static final int TEST_SIZE = 500_000;
    private static final String TEST_NAME = "testmap";
    private final ProtoLongToLongMapAdapter adapter = new ProtoLongToLongMapAdapter();

    @Test
    public void testConsistency()
    {
        final LongToLongMap longMap = new LongToLongMap(TEST_NAME, TEST_SIZE, TEST_SIZE, TEST_SIZE,
                TEST_SIZE, TEST_SIZE, TEST_SIZE);
        for (int index = 0; index < TEST_SIZE; index++)
        {
            final long value = index;
            longMap.put(value, value);
        }

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longMap);
        logger.info("Took {} to serialize LongToLongMap", startTime.elapsedSince());

        startTime = Time.now();
        final LongToLongMap parsedFrom = (LongToLongMap) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongToLongMap from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longMap, parsedFrom);
    }

    @Test
    public void testNullFields()
    {
        final LongToLongMap longMap = new LongToLongMap(null, 10);

        Assert.assertTrue(longMap.getName() == null);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longMap);
        logger.info("Took {} to serialize LongToLongMap", startTime.elapsedSince());

        startTime = Time.now();
        final LongToLongMap parsedFrom = (LongToLongMap) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongToLongMap from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longMap, parsedFrom);
    }
}

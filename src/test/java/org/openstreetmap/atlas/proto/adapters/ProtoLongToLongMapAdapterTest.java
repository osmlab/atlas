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
    private static final long TEST_SIZE = 5_000_000;
    private final ProtoLongToLongMapAdapter adapter = new ProtoLongToLongMapAdapter();

    @Test
    public void testConsistency()
    {
        final LongToLongMap longMap = new LongToLongMap("test", TEST_SIZE);
        for (long l = 0; l < TEST_SIZE; l++)
        {
            longMap.put(l, l);
        }

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(longMap);
        logger.info("Took {} to serialize LongArray", startTime.elapsedSince());

        startTime = Time.now();
        final LongToLongMap parsedFrom = (LongToLongMap) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize LongArray from bytestream", startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(longMap.getMaximumSize(), longMap.getMaximumSize());
        for (long l = 0; l < TEST_SIZE; l++)
        {
            Assert.assertEquals(longMap.get(l), parsedFrom.get(l));
        }
    }
}

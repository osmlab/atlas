package org.openstreetmap.atlas.proto.adapters;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.proto.ProtoPrimitiveLongArray;
import org.openstreetmap.atlas.utilities.arrays.LongArray;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoPrimitiveLongArrayAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoPrimitiveLongArrayAdapterTest.class);

    @Test
    public void testPackUnpackIntegrity()
    {
        final Random random = new Random();
        final int testArraySize = 2;
        final LongArray longArray = new LongArray(testArraySize);
        LongArray unpackedArray;
        ProtoPrimitiveLongArray primitiveLongArray;
        final ProtoPrimitiveLongArrayAdapter adapter = new ProtoPrimitiveLongArrayAdapter();
        for (int i = 0; i < testArraySize; i++)
        {
            longArray.add((long) random.nextInt(10));
        }

        Time currentTime = Time.now();
        primitiveLongArray = adapter.buildProtoPrimitiveLongArray(longArray);
        logger.info("Pack took {}", currentTime.elapsedSince());

        currentTime = Time.now();
        unpackedArray = adapter.unpackProtoPrimitiveLongArray(primitiveLongArray);
        logger.info("Unpack took {}", currentTime.elapsedSince());

        logger.info("Comparing equality...");
        // Assert.assertTrue(longArray.equals(unpackedArray));
        Assert.assertEquals(longArray, unpackedArray);
    }
}

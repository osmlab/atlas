package org.openstreetmap.atlas.proto.adapters;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.proto.ProtoPrimitiveLongArray;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.arrays.LongArray;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoPrimitiveLongArrayAdapterTest
{
    private static final int TEST_ARRAY_SIZE = 1_000_000;
    private static final int RANDOM_CEILING = 10;

    private static final Logger logger = LoggerFactory
            .getLogger(ProtoPrimitiveLongArrayAdapterTest.class);

    @Test
    public void testPackUnpackIntegrity()
    {
        final Random random = new Random();
        final LongArray longArray = new LongArray(TEST_ARRAY_SIZE);
        final LongArray unpackedArray;
        final ProtoPrimitiveLongArray primitiveLongArray;
        final ProtoPrimitiveLongArrayAdapter adapter = new ProtoPrimitiveLongArrayAdapter();
        for (int i = 0; i < TEST_ARRAY_SIZE; i++)
        {
            longArray.add((long) random.nextInt(RANDOM_CEILING));
        }

        Time currentTime = Time.now();
        primitiveLongArray = adapter.buildProtoPrimitiveLongArray(longArray);
        logger.info("Pack took {}", currentTime.elapsedSince());

        currentTime = Time.now();
        unpackedArray = adapter.unpackProtoPrimitiveLongArray(primitiveLongArray);
        logger.info("Unpack took {}", currentTime.elapsedSince());

        logger.info("Comparing equality...");
        Assert.assertEquals(longArray, unpackedArray);

        logger.info("Testing file IO...");
        currentTime = Time.now();
        final File file = File.temporary();
        final byte[] contents = primitiveLongArray.toByteArray();
        file.writeAndClose(contents);
        final byte[] contents2 = file.readBytesAndClose();
        logger.info("File IO roundtrip took {}", currentTime.elapsedSince());
        logger.info("Checking read/write consistency...");
        // TODO assertArrayEquals does not work here because JUnit tries to actually print the
        // array to the console for some reason, totally blowing the heap
        for (int i = 0; i < contents.length; i++)
        {
            Assert.assertEquals(contents[i], contents2[i]);
        }
    }
}

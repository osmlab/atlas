package org.openstreetmap.atlas.proto;

import org.junit.Test;
import org.openstreetmap.atlas.proto.ProtoBoxedLongArray;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoBoxedLongArrayTest
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoBoxedLongArrayTest.class);

    private static final int TEST_SIZE = 7_000_000;

    @Test
    public void test()
    {
        final ProtoBoxedLongArray.Builder arrayBuilder = ProtoBoxedLongArray.newBuilder();

        final Time startTime = Time.now();
        for (int i = 0; i < TEST_SIZE; i++)
        {
            arrayBuilder.addValue(i);
        }
        final ProtoBoxedLongArray array = arrayBuilder.build();
        logger.info("Took {}", startTime.elapsedSince());
        for (int i = 0; i < TEST_SIZE; i++)
        {
            array.getValue(i);
        }
        logger.info("Took {}", startTime.elapsedSince());
    }
}

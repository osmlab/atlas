package org.openstreetmap.atlas.proto;

import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author lcram
 */
public class ProtoBoxedLongArrayTest
{
    private static final Logger logger = LoggerFactory.getLogger(ProtoBoxedLongArrayTest.class);

    private static final int TEST_SIZE = 10_000_000;

    @Test
    public void test()
    {
        final ProtoBoxedLongArray.Builder arrayBuilder = ProtoBoxedLongArray.newBuilder();

        Time startTime = Time.now();
        for (int i = 0; i < TEST_SIZE; i++)
        {
            arrayBuilder.addValue(i);
        }
        ProtoBoxedLongArray array = arrayBuilder.build();
        logger.info("Took {} to build proto object", startTime.elapsedSince());

        startTime = Time.now();
        final File tempFile = File.temporary();
        tempFile.writeAndClose(array.toByteArray());
        logger.info("Took {} to serialize and write to file", startTime.elapsedSince());

        startTime = Time.now();
        final byte[] contents = tempFile.readBytesAndClose();
        try
        {
            array = ProtoBoxedLongArray.parseFrom(contents);
        }
        catch (final InvalidProtocolBufferException e)
        {
            e.printStackTrace();
        }
        logger.info("Took {} to read from file and deserialize", startTime.elapsedSince());
        tempFile.delete();
    }
}

package org.openstreetmap.atlas.utilities.conversion;

import java.nio.ByteBuffer;

import org.openstreetmap.atlas.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert a long to an equivalent Big Endian byte array representation and vice versa.
 * 
 * @author lcram
 */
public class LongToByteArrayConverter implements TwoWayConverter<Long, byte[]>
{
    private static final Logger logger = LoggerFactory.getLogger(LongToByteArrayConverter.class);

    private static final int BYTES_PER_LONG = 8;

    // TODO this has been commented out because it breaks repeated flip() calls in
    // backwardConvert(), figure out why this is
    // declare the buffer statically so we do not have to reallocate on every call to the client
    // methods
    // private static ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);

    @Override
    public Long backwardConvert(final byte[] byteArray)
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        if (byteArray.length != BYTES_PER_LONG)
        {
            throw new CoreException("Byte array must contain exactly " + BYTES_PER_LONG
                    + " bytes to be converted to Long");
        }
        byteBuffer.put(byteArray, 0, byteArray.length);
        byteBuffer.flip();
        return byteBuffer.getLong();
    }

    @Override
    public byte[] convert(final Long longToConvert)
    {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(0, longToConvert);
        return byteBuffer.array();
    }
}

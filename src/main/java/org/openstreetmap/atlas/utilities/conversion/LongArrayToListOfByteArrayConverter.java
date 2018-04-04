package org.openstreetmap.atlas.utilities.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.arrays.LongArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert a LongArray to a List of byte[] in Big Endian byte ordering, or vice versa. This
 * implementation currently places a size restriction on the LongArray.
 *
 * @author lcram
 */
public class LongArrayToListOfByteArrayConverter implements TwoWayConverter<LongArray, List<byte[]>>
{
    private static final Logger logger = LoggerFactory
            .getLogger(LongArrayToListOfByteArrayConverter.class);

    private static final int BYTES_PER_LONG = 8;

    // the maximum allowed size of the LongArray to convert
    // TODO I think this can probably be made larger
    private static final int MAXIMUM_ARRAY_SIZE = 8 * (Integer.MAX_VALUE / 8);

    // TODO need to figure out a reasonable size for this. The tradeoff is that the smaller the
    // buffer size, the larger the list of byte[] is. However, small buffer means fast allocation
    // for packing.
    // NOTE this needs to be a multiple of 8 since longs are 8 bytes, if this is not a multiple of 8
    // then an ArrayIndexOutOfBounds will be thrown by convert()
    private static final int BYTE_BUFFER_SIZE = 8 * 1024;

    private static final LongToByteArrayConverter CONVERTER = new LongToByteArrayConverter();

    @Override
    public LongArray backwardConvert(final List<byte[]> byteArrayList)
    {
        // count the total length in bytes of the sequence, so we can allocate the LongArray
        // accurately
        final long lengthOfByteArrays = byteArrayList.stream().mapToLong(element ->
        {
            return element.length;
        }).sum();

        if (lengthOfByteArrays % BYTES_PER_LONG != 0)
        {
            throw new CoreException("Byte sequence cannot represent a well formed LongArray");
        }

        final LongArray array = new LongArray(lengthOfByteArrays / BYTES_PER_LONG);

        for (final byte[] element : byteArrayList)
        {
            for (int index = 0; index < element.length; index += BYTES_PER_LONG)
            {
                final long result = CONVERTER.backwardConvert(
                        Arrays.copyOfRange(element, index, index + BYTES_PER_LONG));
                array.add(result);
            }
        }

        return array;
    }

    @Override
    public List<byte[]> convert(final LongArray longArray)
    {
        if (longArray.size() > MAXIMUM_ARRAY_SIZE)
        {
            throw new CoreException("Size of array to convert cannot exceed " + MAXIMUM_ARRAY_SIZE);
        }
        final List<byte[]> resultListOfByteArrays = new ArrayList<>(BYTES_PER_LONG);
        byte[] temporaryBuffer = null;
        int bytesWritten = 0;

        for (int indexIntoLongArray = 0; indexIntoLongArray < longArray
                .size(); indexIntoLongArray++)
        {
            if (bytesWritten == 0)
            {
                temporaryBuffer = new byte[BYTE_BUFFER_SIZE];
            }

            final byte[] convertedLongBuffer = CONVERTER.convert(longArray.get(indexIntoLongArray));
            for (int convertedLongBufferIndex = 0; convertedLongBufferIndex < convertedLongBuffer.length; convertedLongBufferIndex++)
            {
                temporaryBuffer[bytesWritten] = convertedLongBuffer[convertedLongBufferIndex];
                bytesWritten++;
            }

            // flush the temporaryBuffer to the list if it is full
            if (bytesWritten >= BYTE_BUFFER_SIZE)
            {
                bytesWritten = 0;
                resultListOfByteArrays.add(temporaryBuffer);
            }
        }
        // there are still some unflushed data in the temporary buffer, trim the temporary buffer
        // and flush to the list
        if (bytesWritten > 0)
        {
            final byte[] temporaryTrimmedBuffer = new byte[bytesWritten];
            System.arraycopy(temporaryBuffer, 0, temporaryTrimmedBuffer, 0, bytesWritten);
            resultListOfByteArrays.add(temporaryTrimmedBuffer);
        }

        return resultListOfByteArrays;
    }
}

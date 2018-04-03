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

    // round Integer.MAX_VALUE down to closest multiple of 8, this will make packing/unpacking logic
    // much simpler
    private static final int MAXIMUM_ARRAY_SIZE = 8 * (Integer.MAX_VALUE / 8);

    private static final LongToByteArrayConverter CONVERTER = new LongToByteArrayConverter();

    @Override
    public LongArray backwardConvert(final List<byte[]> byteArrayList)
    {
        long lengthOfByteArrays = 0;

        // count the total length in bytes of the sequence, so we can allocate the LongArray
        // accurately
        for (final byte[] element : byteArrayList)
        {
            lengthOfByteArrays += element.length;
        }
        if (lengthOfByteArrays % 8 != 0)
        {
            throw new CoreException("Byte sequence cannot represent a well formed LongArray");
        }

        final LongArray array = new LongArray(lengthOfByteArrays / 8);

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
        // TODO this may not be necessary
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
                // TODO this OOMs on relatively low LongArray sizes, fix?
                temporaryBuffer = new byte[MAXIMUM_ARRAY_SIZE];
            }

            final byte[] convertedLongBuffer = CONVERTER.convert(longArray.get(indexIntoLongArray));
            for (int convertedLongBufferIndex = 0; convertedLongBufferIndex < convertedLongBuffer.length; convertedLongBufferIndex++)
            {
                temporaryBuffer[bytesWritten] = convertedLongBuffer[convertedLongBufferIndex];
            }
            bytesWritten += BYTES_PER_LONG;

            // flush the temporaryBuffer to the list upon full
            if (bytesWritten >= MAXIMUM_ARRAY_SIZE)
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

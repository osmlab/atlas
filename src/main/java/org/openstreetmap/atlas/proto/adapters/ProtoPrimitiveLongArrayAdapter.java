package org.openstreetmap.atlas.proto.adapters;

import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.proto.ProtoPrimitiveLongArray;
import org.openstreetmap.atlas.utilities.arrays.LongArray;
import org.openstreetmap.atlas.utilities.conversion.LongArrayToListOfByteArrayConverter;

import com.google.protobuf.ByteString;

/**
 * Provides an interface for client code to obtain a ready_to_serialize LongArray in the form of a
 * ProtoPrimitiveLongArray. Also provides a method to obtain a LongArray back from a
 * ProtoPrimitiveLongArray.
 *
 * @author lcram
 */
public class ProtoPrimitiveLongArrayAdapter
{
    // TODO perhaps this class would be better implemented as a TwoWayConverter and not an Adapter?

    private static final LongArrayToListOfByteArrayConverter CONVERTER = new LongArrayToListOfByteArrayConverter();

    public ProtoPrimitiveLongArray buildProtoPrimitiveLongArray(final LongArray array)
    {
        final ProtoPrimitiveLongArray.Builder arrayBuilder = ProtoPrimitiveLongArray.newBuilder();
        arrayBuilder.setNumberOfElements(array.size());

        final List<byte[]> byteArrays = CONVERTER.convert(array);
        for (final byte[] element : byteArrays)
        {
            arrayBuilder.addChunk(ByteString.copyFrom(element));
        }

        return arrayBuilder.build();
    }

    public LongArray unpackProtoPrimitiveLongArray(final ProtoPrimitiveLongArray array)
    {
        final List<byte[]> byteArrays = array.getChunkList().stream().map(ByteString::toByteArray)
                .collect(Collectors.toList());

        return CONVERTER.backwardConvert(byteArrays);
    }
}

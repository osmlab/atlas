package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoIntegerArrayOfArrays;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.converters.ProtoIntegerArrayOfArraysConverter;
import org.openstreetmap.atlas.utilities.arrays.IntegerArrayOfArrays;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link IntegerArrayOfArrays} and
 * {@link ProtoIntegerArrayOfArrays}.
 *
 * @author lcram
 */
public class ProtoIntegerArrayOfArraysAdapter implements ProtoAdapter
{
    private static final ProtoIntegerArrayOfArraysConverter CONVERTER = new ProtoIntegerArrayOfArraysConverter();

    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoIntegerArrayOfArrays protoIntegerArrayOfArrays = null;
        try
        {
            protoIntegerArrayOfArrays = ProtoIntegerArrayOfArrays.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }

        final IntegerArrayOfArrays integerArrayOfArrays = CONVERTER
                .convert(protoIntegerArrayOfArrays);

        return integerArrayOfArrays;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof IntegerArrayOfArrays))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final IntegerArrayOfArrays integerArrayOfArrays = (IntegerArrayOfArrays) serializable;

        if (integerArrayOfArrays.size() > Integer.MAX_VALUE)
        {
            // TODO see note about this check in ProtoLongArrayOfArraysAdapter
            throw new CoreException("Cannot serialize provided {}, size {} too large",
                    serializable.getClass().getName(), integerArrayOfArrays.size());
        }

        final ProtoIntegerArrayOfArrays protoArrays = CONVERTER
                .backwardConvert(integerArrayOfArrays);

        return protoArrays.toByteArray();
    }
}

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

        ProtoIntegerArrayOfArrays protoArrays = null;
        try
        {
            protoArrays = CONVERTER.backwardConvert(integerArrayOfArrays);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Failed to serialize {}",
                    integerArrayOfArrays.getClass().getName(), exception);
        }

        return protoArrays.toByteArray();
    }
}

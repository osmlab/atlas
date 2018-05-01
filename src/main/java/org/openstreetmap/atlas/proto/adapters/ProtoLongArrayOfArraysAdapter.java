package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoLongArray;
import org.openstreetmap.atlas.proto.ProtoLongArrayOfArrays;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.arrays.LongArrayOfArrays;

import com.google.common.primitives.Longs;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link LongArrayOfArrays} and
 * {@link ProtoLongArrayOfArrays}.
 *
 * @author lcram
 */
public class ProtoLongArrayOfArraysAdapter implements ProtoAdapter
{
    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoLongArrayOfArrays protoLongArrayOfArrays = null;
        try
        {
            protoLongArrayOfArrays = ProtoLongArrayOfArrays.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }

        final LongArrayOfArrays longArrayOfArrays = new LongArrayOfArrays(
                protoLongArrayOfArrays.getArraysCount(), protoLongArrayOfArrays.getArraysCount(),
                protoLongArrayOfArrays.getArraysCount());
        if (protoLongArrayOfArrays.hasName())
        {
            longArrayOfArrays.setName(protoLongArrayOfArrays.getName());
        }
        for (int index = 0; index < protoLongArrayOfArrays.getArraysCount(); index++)
        {
            final long[] items = Longs
                    .toArray(protoLongArrayOfArrays.getArrays(index).getElementsList());
            longArrayOfArrays.add(items);
        }

        return longArrayOfArrays;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof LongArrayOfArrays))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final LongArrayOfArrays longArrayOfArrays = (LongArrayOfArrays) serializable;

        if (longArrayOfArrays.size() > Integer.MAX_VALUE)
        {
            throw new CoreException("Cannot serialize {}, size too large ({})",
                    longArrayOfArrays.getClass().getName(), longArrayOfArrays.size());
        }

        final ProtoLongArrayOfArrays.Builder protoArraysBuilder = ProtoLongArrayOfArrays
                .newBuilder();
        for (int index = 0; index < longArrayOfArrays.size(); index++)
        {
            final ProtoLongArray.Builder subArrayBuilder = ProtoLongArray.newBuilder();
            final long[] subArray = longArrayOfArrays.get(index);
            if (subArray == null)
            {
                throw new CoreException("{} cannot serialize arrays with null elements",
                        this.getClass().getName());
            }
            for (int subIndex = 0; subIndex < subArray.length; subIndex++)
            {
                subArrayBuilder.addElements(subArray[subIndex]);
            }
            protoArraysBuilder.addArrays(subArrayBuilder);
        }
        if (longArrayOfArrays.getName() != null)
        {
            protoArraysBuilder.setName(longArrayOfArrays.getName());
        }

        return protoArraysBuilder.build().toByteArray();
    }
}

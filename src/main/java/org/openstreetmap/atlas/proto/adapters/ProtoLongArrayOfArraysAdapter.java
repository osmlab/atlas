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
        protoLongArrayOfArrays.getArraysList().stream().forEach(array ->
        {
            final long[] items = Longs.toArray(array.getElementsList());
            longArrayOfArrays.add(items);
        });

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
        for (final long[] elementArray : longArrayOfArrays)
        {
            final ProtoLongArray.Builder elementArrayBuilder = ProtoLongArray.newBuilder();
            if (elementArray == null)
            {
                throw new CoreException("{} cannot serialize arrays with null elements",
                        this.getClass().getName());
            }
            for (int subIndex = 0; subIndex < elementArray.length; subIndex++)
            {
                elementArrayBuilder.addElements(elementArray[subIndex]);
            }
            protoArraysBuilder.addArrays(elementArrayBuilder);
        }

        if (longArrayOfArrays.getName() != null)
        {
            protoArraysBuilder.setName(longArrayOfArrays.getName());
        }

        return protoArraysBuilder.build().toByteArray();
    }
}

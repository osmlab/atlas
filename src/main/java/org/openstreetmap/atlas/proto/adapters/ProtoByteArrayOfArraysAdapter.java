package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoByteArray;
import org.openstreetmap.atlas.proto.ProtoByteArrayOfArrays;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.arrays.ByteArrayOfArrays;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link ByteArrayOfArrays} and
 * {@link ProtoByteArrayOfArrays}.
 *
 * @author lcram
 */
public class ProtoByteArrayOfArraysAdapter implements ProtoAdapter
{
    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoByteArrayOfArrays protoByteArrayOfArrays = null;
        try
        {
            protoByteArrayOfArrays = ProtoByteArrayOfArrays.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }

        final ByteArrayOfArrays byteArrayOfArrays = new ByteArrayOfArrays(
                protoByteArrayOfArrays.getArraysCount());
        if (protoByteArrayOfArrays.hasName())
        {
            byteArrayOfArrays.setName(protoByteArrayOfArrays.getName());
        }
        for (int index = 0; index < protoByteArrayOfArrays.getArraysCount(); index++)
        {
            final byte[] items = protoByteArrayOfArrays.getArrays(index).getElements()
                    .toByteArray();
            byteArrayOfArrays.add(items);
        }

        return byteArrayOfArrays;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof ByteArrayOfArrays))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final ByteArrayOfArrays byteArrayOfArrays = (ByteArrayOfArrays) serializable;

        if (byteArrayOfArrays.size() > Integer.MAX_VALUE)
        {
            throw new CoreException("Cannot serialize {}, size too large ({})",
                    byteArrayOfArrays.getClass().getName(), byteArrayOfArrays.size());
        }

        final ProtoByteArrayOfArrays.Builder protoArraysBuilder = ProtoByteArrayOfArrays
                .newBuilder();
        for (int index = 0; index < byteArrayOfArrays.size(); index++)
        {
            final ProtoByteArray.Builder subArrayBuilder = ProtoByteArray.newBuilder();
            final byte[] subArray = byteArrayOfArrays.get(index);
            if (subArray == null)
            {
                throw new CoreException("{} cannot serialize arrays with null elements",
                        this.getClass().getName());
            }
            subArrayBuilder.setElements(ByteString.copyFrom(subArray));
            protoArraysBuilder.addArrays(subArrayBuilder);
        }
        if (byteArrayOfArrays.getName() != null)
        {
            protoArraysBuilder.setName(byteArrayOfArrays.getName());
        }

        return protoArraysBuilder.build().toByteArray();
    }
}

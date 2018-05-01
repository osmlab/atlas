package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoLongArray;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.arrays.LongArray;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link LongArray} and
 * {@link ProtoLongArray}.
 *
 * @author lcram
 */
public class ProtoLongArrayAdapter implements ProtoAdapter
{
    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoLongArray protoLongArray = null;
        try
        {
            protoLongArray = ProtoLongArray.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }

        final int elementCount = protoLongArray.getElementsCount();
        final LongArray longArray = new LongArray(elementCount, elementCount, elementCount);
        protoLongArray.getElementsList().stream().forEach(element ->
        {
            longArray.add(element);
        });

        if (protoLongArray.hasName())
        {
            longArray.setName(protoLongArray.getName());
        }

        return longArray;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof LongArray))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final LongArray longArray = (LongArray) serializable;

        if (longArray.size() > Integer.MAX_VALUE)
        {
            throw new CoreException("Cannot serialize {}, size too large ({})",
                    longArray.getClass().getName(), longArray.size());
        }

        final ProtoLongArray.Builder protoLongArrayBuilder = ProtoLongArray.newBuilder();
        for (final long element : longArray)
        {
            protoLongArrayBuilder.addElements(element);
        }

        if (longArray.getName() != null)
        {
            protoLongArrayBuilder.setName(longArray.getName());
        }

        return protoLongArrayBuilder.build().toByteArray();
    }
}

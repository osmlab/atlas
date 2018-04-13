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
        for (int i = 0; i < protoLongArray.getElementsCount(); i++)
        {
            longArray.add(protoLongArray.getElements(i));
        }
        longArray.setName(protoLongArray.getName());

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
            throw new CoreException("Cannot serialize provided {}, size {} too large",
                    serializable.getClass().getName(), longArray.size());
        }

        final ProtoLongArray.Builder builder = ProtoLongArray.newBuilder();
        for (int i = 0; i < longArray.size(); i++)
        {
            builder.addElements(longArray.get(i));
        }
        final String name = longArray.getName() == null ? "" : longArray.getName();
        builder.setName(name);

        final ProtoLongArray protoLongArray = builder.build();
        return protoLongArray.toByteArray();
    }
}

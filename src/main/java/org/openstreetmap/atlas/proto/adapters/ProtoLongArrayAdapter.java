package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoLongArray;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.arrays.LongArray;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect LongArray and ProtoLongArray.
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
        catch (final InvalidProtocolBufferException excep)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream", excep);
        }

        final LongArray longArray = new LongArray(protoLongArray.getElementsCount());
        for (int i = 0; i < protoLongArray.getElementsCount(); i++)
        {
            longArray.add(protoLongArray.getElements(i));
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

        final ProtoLongArray.Builder builder = ProtoLongArray.newBuilder();
        for (int i = 0; i < longArray.size(); i++)
        {
            builder.addElements(longArray.get(i));
        }

        final ProtoLongArray protoLongArray = builder.build();
        return protoLongArray.toByteArray();
    }
}

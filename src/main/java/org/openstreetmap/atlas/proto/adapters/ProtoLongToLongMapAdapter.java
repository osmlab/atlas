package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoLongArray;
import org.openstreetmap.atlas.proto.ProtoLongToLongMap;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.maps.LongToLongMap;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link LongToLongMap} and
 * {@link ProtoLongToLongMap}.
 *
 * @author lcram
 */
public class ProtoLongToLongMapAdapter implements ProtoAdapter
{
    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoLongToLongMap protoLongToLongMap = null;
        try
        {
            protoLongToLongMap = ProtoLongToLongMap.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }

        String deserializedName = null;
        if (protoLongToLongMap.hasName())
        {
            deserializedName = protoLongToLongMap.getName();
        }

        // final int size = protoLongToLongMultiMap.getKeys().getElementsCount();
        // TODO fix this egregious hack
        final int hackSize = 1024 * 1024 * 24;
        final LongToLongMap longToLongMap = new LongToLongMap(deserializedName, hackSize);

        for (int index = 0; index < protoLongToLongMap.getKeys().getElementsCount(); index++)
        {
            longToLongMap.put(protoLongToLongMap.getKeys().getElements(index),
                    protoLongToLongMap.getValues().getElements(index));
        }

        return longToLongMap;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof LongToLongMap))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final LongToLongMap longMap = (LongToLongMap) serializable;

        if (longMap.size() > Integer.MAX_VALUE)
        {
            throw new CoreException("Cannot serialize provided {}, size {} too large",
                    serializable.getClass().getName(), longMap.size());
        }

        final ProtoLongToLongMap.Builder protoMapBuilder = ProtoLongToLongMap.newBuilder();
        final ProtoLongArray.Builder keysBuilder = ProtoLongArray.newBuilder();
        final ProtoLongArray.Builder valuesBuilder = ProtoLongArray.newBuilder();

        final Iterable<Long> iterable = () -> longMap.iterator();
        for (final Long key : iterable)
        {
            final Long value = longMap.get(key);
            keysBuilder.addElements(key);
            valuesBuilder.addElements(value);
        }
        protoMapBuilder.setKeys(keysBuilder);
        protoMapBuilder.setValues(valuesBuilder);
        if (longMap.getName() != null)
        {
            protoMapBuilder.setName(longMap.getName());
        }

        return protoMapBuilder.build().toByteArray();
    }
}

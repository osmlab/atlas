package org.openstreetmap.atlas.proto.adapters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.proto.ProtoLongArray;
import org.openstreetmap.atlas.proto.ProtoLongToLongMultiMap;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.utilities.maps.LongToLongMultiMap;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link LongToLongMultiMap} and
 * {@link ProtoLongToLongMultiMap}.
 *
 * @author lcram
 */
public class ProtoLongToLongMultiMapAdapter implements ProtoAdapter
{
    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoLongToLongMultiMap protoLongToLongMultiMap = null;
        try
        {
            protoLongToLongMultiMap = ProtoLongToLongMultiMap.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }

        // final int size = protoLongToLongMultiMap.getKeys().getElementsCount();
        // TODO fix this egregious hack
        final int hackSize = 1024 * 1024 * 24;
        final LongToLongMultiMap longToLongMultiMap = new LongToLongMultiMap(
                protoLongToLongMultiMap.getName(), hackSize);

        for (int index = 0; index < protoLongToLongMultiMap.getKeys().getElementsCount(); index++)
        {
            // First we get the proto format array associated with this key
            final ProtoLongArray protoLongArray = protoLongToLongMultiMap.getValuesList()
                    .get(index);
            // Now we convert this proto format array to a List<Long> and then to a primitive long[]
            final long[] values = protoLongArray.getElementsList().stream()
                    .mapToLong(Long::longValue).toArray();
            longToLongMultiMap.put(protoLongToLongMultiMap.getKeys().getElements(index), values);
        }

        return longToLongMultiMap;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof LongToLongMultiMap))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final LongToLongMultiMap longMultiMap = (LongToLongMultiMap) serializable;

        if (longMultiMap.size() > Integer.MAX_VALUE)
        {
            throw new CoreException("Cannot serialize provided {}, size {} too large",
                    serializable.getClass().getName(), longMultiMap.size());
        }

        final ProtoLongToLongMultiMap.Builder protoMapBuilder = ProtoLongToLongMultiMap
                .newBuilder();
        final ProtoLongArray.Builder keysBuilder = ProtoLongArray.newBuilder();

        final Iterable<Long> iterable = () -> longMultiMap.iterator();
        for (final Long key : iterable)
        {
            final ProtoLongArray.Builder valuesBuilder = ProtoLongArray.newBuilder();
            final long[] value = longMultiMap.get(key);
            keysBuilder.addElements(key);
            for (int index = 0; index < value.length; index++)
            {
                valuesBuilder.addElements(value[index]);
            }
            protoMapBuilder.addValues(valuesBuilder);
        }
        protoMapBuilder.setKeys(keysBuilder);
        final String name = longMultiMap.getName() == null ? "" : longMultiMap.getName();
        protoMapBuilder.setName(name);

        return protoMapBuilder.build().toByteArray();
    }
}

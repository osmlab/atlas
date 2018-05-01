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
    /*
     * If the maximum size of the LongToLongMap we are reading is less than this value, just use
     * this value instead of the actual max size. This ensures that calculations in LargeMap using
     * DEFAULT_HASH_MODULO_RATIO do not fail do to integer division truncation.
     */
    private static final int DEFAULT_MAX_SIZE = 1024;

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

        String deserializedName = null;
        if (protoLongToLongMultiMap.hasName())
        {
            deserializedName = protoLongToLongMultiMap.getName();
        }

        final int size = protoLongToLongMultiMap.getKeys().getElementsCount() <= DEFAULT_MAX_SIZE
                ? DEFAULT_MAX_SIZE : protoLongToLongMultiMap.getKeys().getElementsCount();
        final LongToLongMultiMap longToLongMultiMap = new LongToLongMultiMap(deserializedName, size,
                size, size, size, size, size);

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
            throw new CoreException("Cannot serialize {}, size too large ({})",
                    longMultiMap.getClass().getName(), longMultiMap.size());
        }

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
            if (value == null)
            {
                throw new CoreException("{} cannot serialize arrays with null elements",
                        this.getClass().getName());
            }
            keysBuilder.addElements(key);
            for (int index = 0; index < value.length; index++)
            {
                valuesBuilder.addElements(value[index]);
            }
            protoMapBuilder.addValues(valuesBuilder);
        }
        protoMapBuilder.setKeys(keysBuilder);
        if (longMultiMap.getName() != null)
        {
            protoMapBuilder.setName(longMultiMap.getName());
        }

        return protoMapBuilder.build().toByteArray();
    }
}

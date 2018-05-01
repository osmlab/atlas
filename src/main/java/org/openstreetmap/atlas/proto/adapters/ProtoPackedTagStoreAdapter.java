package org.openstreetmap.atlas.proto.adapters;

import java.lang.reflect.Field;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.packed.PackedTagStore;
import org.openstreetmap.atlas.proto.ProtoIntegerArrayOfArrays;
import org.openstreetmap.atlas.proto.ProtoPackedTagStore;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.converters.ProtoIntegerArrayOfArraysConverter;
import org.openstreetmap.atlas.utilities.arrays.IntegerArrayOfArrays;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Implements the {@link ProtoAdapter} interface to connect {@link PackedTagStore} and
 * {@link ProtoPackedTagStore}.
 *
 * @author lcram
 */
public class ProtoPackedTagStoreAdapter implements ProtoAdapter
{
    /*
     * PackedTagStore does not provide an interface for setting its arrays directly. This class's
     * implementation uses reflection to side-step the issue. Unlike the other PackedAtlas field
     * classes, there is no nice converter for the ProtoPackedTagStore (since it cannot be rebuilt
     * through its public API).
     */

    private static final ProtoIntegerArrayOfArraysConverter CONVERTER = new ProtoIntegerArrayOfArraysConverter();

    @Override
    public ProtoSerializable deserialize(final byte[] byteArray)
    {
        ProtoPackedTagStore protoStore = null;
        try
        {
            protoStore = ProtoPackedTagStore.parseFrom(byteArray);
        }
        catch (final InvalidProtocolBufferException exception)
        {
            throw new CoreException("Error encountered while parsing protobuf bytestream",
                    exception);
        }
        final PackedTagStore store = new PackedTagStore();

        final ProtoIntegerArrayOfArrays protoKeyArray = protoStore.getKeys();
        final ProtoIntegerArrayOfArrays protoValueArray = protoStore.getValues();

        final IntegerArrayOfArrays keyArray = CONVERTER.convert(protoKeyArray);
        final IntegerArrayOfArrays valueArray = CONVERTER.convert(protoValueArray);
        final Long index = protoStore.getIndex();

        Field keyArrayField = null;
        Field valueArrayField = null;
        Field indexField = null;

        try
        {
            keyArrayField = store.getClass().getDeclaredField(PackedTagStore.FIELD_KEYS);
            keyArrayField.setAccessible(true);
            keyArrayField.set(store, keyArray);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to set field \"{}\" in {}", PackedTagStore.FIELD_KEYS,
                    store.getClass().getName(), exception);
        }

        try
        {
            valueArrayField = store.getClass().getDeclaredField(PackedTagStore.FIELD_VALUES);
            valueArrayField.setAccessible(true);
            valueArrayField.set(store, valueArray);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to set field \"{}\" in {}", PackedTagStore.FIELD_VALUES,
                    store.getClass().getName(), exception);
        }

        try
        {
            indexField = store.getClass().getDeclaredField(PackedTagStore.FIELD_INDEX);
            indexField.setAccessible(true);
            indexField.set(store, index);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to set field \"{}\" in {}", PackedTagStore.FIELD_INDEX,
                    store.getClass().getName(), exception);
        }

        return store;
    }

    @Override
    public byte[] serialize(final ProtoSerializable serializable)
    {
        if (!(serializable instanceof PackedTagStore))
        {
            throw new CoreException(
                    "Invalid ProtoSerializable type was provided to {}: cannot serialize {}",
                    this.getClass().getName(), serializable.getClass().getName());
        }
        final PackedTagStore store = (PackedTagStore) serializable;

        final ProtoPackedTagStore.Builder protoTagStoreBuilder = ProtoPackedTagStore.newBuilder();

        Field keyArrayField = null;
        IntegerArrayOfArrays keyArray = null;
        Field valueArrayField = null;
        IntegerArrayOfArrays valueArray = null;
        Field indexField = null;
        Long index = -1L;

        try
        {
            keyArrayField = store.getClass().getDeclaredField(PackedTagStore.FIELD_KEYS);
            keyArrayField.setAccessible(true);
            keyArray = (IntegerArrayOfArrays) keyArrayField.get(store);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to read field \"{}\" from {}",
                    PackedTagStore.FIELD_KEYS, store.getClass().getName(), exception);
        }

        try
        {
            valueArrayField = store.getClass().getDeclaredField(PackedTagStore.FIELD_VALUES);
            valueArrayField.setAccessible(true);
            valueArray = (IntegerArrayOfArrays) valueArrayField.get(store);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to read field \"{}\" from {}",
                    PackedTagStore.FIELD_VALUES, store.getClass().getName(), exception);
        }

        try
        {
            indexField = store.getClass().getDeclaredField(PackedTagStore.FIELD_INDEX);
            indexField.setAccessible(true);
            index = (Long) indexField.get(store);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to read field \"{}\" from {}",
                    PackedTagStore.FIELD_INDEX, store.getClass().getName(), exception);
        }

        try
        {
            protoTagStoreBuilder.setKeys(CONVERTER.backwardConvert(keyArray));
        }
        catch (final Exception exception)
        {
            throw new CoreException("Failed to serialize {}", keyArray.getClass().getName(),
                    exception);
        }
        try
        {
            protoTagStoreBuilder.setValues(CONVERTER.backwardConvert(valueArray));
        }
        catch (final Exception exception)
        {
            throw new CoreException("Failed to serialize {}", valueArray.getClass().getName(),
                    exception);
        }
        protoTagStoreBuilder.setIndex(index);

        return protoTagStoreBuilder.build().toByteArray();
    }
}

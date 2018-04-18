package org.openstreetmap.atlas.utilities.maps;

import java.util.Objects;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasSerializer;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoLongToLongMultiMapAdapter;
import org.openstreetmap.atlas.utilities.arrays.Arrays;
import org.openstreetmap.atlas.utilities.arrays.LargeArray;
import org.openstreetmap.atlas.utilities.arrays.LongArray;
import org.openstreetmap.atlas.utilities.arrays.LongArrayOfArrays;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * {@link LargeMap} long to long[]
 *
 * @author matthieun
 * @author lcram
 */
public class LongToLongMultiMap extends LargeMap<Long, long[]> implements ProtoSerializable
{
    private static final long serialVersionUID = 6741833447370296269L;

    /**
     * @param maximumSize
     *            The maximum number of keys in the map
     */
    public LongToLongMultiMap(final long maximumSize)
    {
        super(maximumSize);
    }

    /**
     * @param maximumSize
     *            The maximum number of keys in the map
     * @param hashSize
     *            The size of the hash domain
     */
    public LongToLongMultiMap(final long maximumSize, final int hashSize)
    {
        super(maximumSize, hashSize);
    }

    /**
     * @param name
     *            The name of the map
     * @param maximumSize
     *            The maximum number of keys in the map
     */
    public LongToLongMultiMap(final String name, final long maximumSize)
    {
        super(name, maximumSize);
    }

    /**
     * @param name
     *            The name of the map
     * @param maximumSize
     *            The maximum number of keys in the map
     * @param hashSize
     *            The size of the hash domain
     * @param keyMemoryBlockSize
     *            The initial memory allocation size for the keys sub arrays
     * @param keySubArraySize
     *            The maximum size of a keys sub array
     * @param valueMemoryBlockSize
     *            The initial memory allocation size for the values sub arrays
     * @param valueSubArraySize
     *            The maximum size of a values sub array
     */
    public LongToLongMultiMap(final String name, final long maximumSize, final int hashSize,
            final int keyMemoryBlockSize, final int keySubArraySize, final int valueMemoryBlockSize,
            final int valueSubArraySize)
    {
        super(name, maximumSize, hashSize, keyMemoryBlockSize, keySubArraySize,
                valueMemoryBlockSize, valueSubArraySize);
    }

    /**
     * This nullary constructor is solely for use by the {@link PackedAtlasSerializer}, which calls
     * it using reflection. It allows the serializer code to obtain a handle on a
     * {@link LongToLongMultiMap} that it can use to grab the correct {@link ProtoAdapter}. The
     * object initialized with this constructor will be corrupted for general use and should be
     * discarded.
     */
    @SuppressWarnings("unused")
    private LongToLongMultiMap()
    {
        super();
    }

    public void add(final Long key, final long value)
    {
        final long[] result;
        if (this.containsKey(key))
        {
            result = Arrays.addNewItem(this.get(key), value);
        }
        else
        {
            result = new long[1];
            result[0] = value;
        }
        this.put(key, result);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof LongToLongMultiMap)
        {
            if (this == other)
            {
                return true;
            }
            final LongToLongMultiMap that = (LongToLongMultiMap) other;
            if (!Objects.equals(this.getName(), that.getName()))
            {
                return false;
            }
            if (this.size() != that.size())
            {
                return false;
            }
            final Iterable<Long> iterable = () -> this.iterator();
            for (final Long key : iterable)
            {
                if (!that.containsKey(key))
                {
                    return false;
                }
                final long[] thisValue = this.get(key);
                final long[] thatValue = that.get(key);
                if (thisValue.length != thatValue.length)
                {
                    return false;
                }
                for (int index = 0; index < thisValue.length; index++)
                {
                    if (thisValue[index] != thatValue[index])
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoLongToLongMultiMapAdapter();
    }

    /*
     * This hashcode implementation bases the hash on a deeply computed value from the long[]
     * contents. This is almost certainly undesirable for performance purposes, but it does maintain
     * the standard relationship between equals() and hashCode() on given objects (namely that two
     * objects which satisfy equals() will share a hashCode()). Note that this method will likely
     * never be used and only exists because Checkstyle forces it to be here.
     */
    @Override
    public int hashCode()
    {
        final int initialPrime = 31;
        final int hashSeed = 37;

        final int nameHash = this.getName() == null ? 0 : this.getName().hashCode();
        int hash = hashSeed * initialPrime + nameHash;
        hash = hashSeed * hash + Long.valueOf(this.size()).hashCode();

        final Iterable<Long> iterable = () -> this.iterator();
        for (final Long key : iterable)
        {
            final long[] value = this.get(key);
            for (int index = 0; index < value.length; index++)
            {
                hash = hashSeed * hash + Long.valueOf(value[index]).hashCode();
            }
        }

        return hash;
    }

    @Override
    protected String asValueString(final long[] value)
    {
        final StringList list = new StringList();
        for (final long val : value)
        {
            list.add(val);
        }
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(list.join(", "));
        builder.append("]");
        return builder.toString();
    }

    @Override
    protected LargeArray<Long> createKeys(final int memoryBlockSize, final int subArraySize)
    {
        final LongArray result;
        if (memoryBlockSize > 0 && subArraySize > 0)
        {
            result = new LongArray(this.getMaximumSize(), memoryBlockSize, subArraySize);
        }
        else
        {
            result = new LongArray(this.getMaximumSize());
        }
        result.withName(getName() + " - Keys");
        return result;
    }

    @Override
    protected LargeArray<long[]> createValues(final int memoryBlockSize, final int subArraySize)
    {
        final LongArrayOfArrays result;
        if (memoryBlockSize > 0 && subArraySize > 0)
        {
            result = new LongArrayOfArrays(this.getMaximumSize(), memoryBlockSize, subArraySize);
        }
        else
        {
            result = new LongArrayOfArrays(this.getMaximumSize());
        }
        result.withName(getName() + " - Values");
        return result;
    }
}

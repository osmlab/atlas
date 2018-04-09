package org.openstreetmap.atlas.utilities.maps;

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
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoLongToLongMultiMapAdapter();
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

package org.openstreetmap.atlas.utilities.maps;

import org.openstreetmap.atlas.utilities.arrays.Arrays;
import org.openstreetmap.atlas.utilities.arrays.IntegerArrayOfArrays;
import org.openstreetmap.atlas.utilities.arrays.LargeArray;
import org.openstreetmap.atlas.utilities.arrays.LongArray;

/**
 * {@link LargeMap} long to int[]
 *
 * @author matthieun
 */
public class LongToIntegerMultiMap extends LargeMap<Long, int[]>
{
    private static final long serialVersionUID = 6741833447370296269L;

    /**
     * @param maximumSize
     *            The maximum number of keys in the map
     */
    public LongToIntegerMultiMap(final long maximumSize)
    {
        super(maximumSize);
    }

    /**
     * @param maximumSize
     *            The maximum number of keys in the map
     * @param hashSize
     *            The size of the hash domain
     */
    public LongToIntegerMultiMap(final long maximumSize, final int hashSize)
    {
        super(maximumSize, hashSize);
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
    public LongToIntegerMultiMap(final String name, final long maximumSize, final int hashSize,
            final int keyMemoryBlockSize, final int keySubArraySize, final int valueMemoryBlockSize,
            final int valueSubArraySize)
    {
        super(name, maximumSize, hashSize, keyMemoryBlockSize, keySubArraySize,
                valueMemoryBlockSize, valueSubArraySize);
    }

    public void add(final Long key, final int value)
    {
        final int[] result;
        if (this.containsKey(key))
        {
            result = Arrays.addNewItem(this.get(key), value);
        }
        else
        {
            result = new int[1];
            result[0] = value;
        }
        this.put(key, result);
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
    protected LargeArray<int[]> createValues(final int memoryBlockSize, final int subArraySize)
    {
        final IntegerArrayOfArrays result;
        if (memoryBlockSize > 0 && subArraySize > 0)
        {
            result = new IntegerArrayOfArrays(this.getMaximumSize(), memoryBlockSize, subArraySize);
        }
        else
        {
            result = new IntegerArrayOfArrays(this.getMaximumSize());
        }
        result.withName(getName() + " - Values");
        return result;
    }
}

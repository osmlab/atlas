package org.openstreetmap.atlas.utilities.maps;

import org.openstreetmap.atlas.utilities.arrays.IntegerArray;
import org.openstreetmap.atlas.utilities.arrays.LargeArray;

/**
 * @author matthieun
 */
public class IntegerToIntegerMap extends LargeMap<Integer, Integer>
{
    private static final long serialVersionUID = -3202467180188514944L;

    /**
     * @param maximumSize
     *            The maximum number of keys in the map
     */
    public IntegerToIntegerMap(final long maximumSize)
    {
        super(maximumSize);
    }

    /**
     * @param maximumSize
     *            The maximum number of keys in the map
     * @param hashSize
     *            The size of the hash domain
     */
    public IntegerToIntegerMap(final long maximumSize, final int hashSize)
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
    public IntegerToIntegerMap(final String name, final long maximumSize, final int hashSize,
            final int keyMemoryBlockSize, final int keySubArraySize, final int valueMemoryBlockSize,
            final int valueSubArraySize)
    {
        super(name, maximumSize, hashSize, keyMemoryBlockSize, keySubArraySize,
                valueMemoryBlockSize, valueSubArraySize);
    }

    @Override
    protected LargeArray<Integer> createKeys(final int memoryBlockSize, final int subArraySize)
    {
        final IntegerArray result;
        if (memoryBlockSize > 0 && subArraySize > 0)
        {
            result = new IntegerArray(this.getMaximumSize(), memoryBlockSize, subArraySize);
        }
        else
        {
            result = new IntegerArray(this.getMaximumSize());
        }
        result.withName(getName() + " - Keys");
        return result;
    }

    @Override
    protected LargeArray<Integer> createValues(final int memoryBlockSize, final int subArraySize)
    {
        final IntegerArray result;
        if (memoryBlockSize > 0 && subArraySize > 0)
        {
            result = new IntegerArray(this.getMaximumSize(), memoryBlockSize, subArraySize);
        }
        else
        {
            result = new IntegerArray(this.getMaximumSize());
        }
        result.withName(getName() + " - Values");
        return result;
    }
}

package org.openstreetmap.atlas.utilities.maps;

import org.openstreetmap.atlas.utilities.arrays.IntegerArray;
import org.openstreetmap.atlas.utilities.arrays.LargeArray;
import org.openstreetmap.atlas.utilities.arrays.LongArray;

/**
 * Large Map from Long to Integer
 *
 * @author matthieun
 */
public class LongToIntegerMap extends LargeMap<Long, Integer>
{
    private static final long serialVersionUID = 346161750484489066L;

    /**
     * @param maximumSize
     *            The maximum number of keys in the map
     */
    public LongToIntegerMap(final long maximumSize)
    {
        super(maximumSize);
    }

    /**
     * @param maximumSize
     *            The maximum number of keys in the map
     * @param hashSize
     *            The size of the hash domain
     */
    public LongToIntegerMap(final long maximumSize, final int hashSize)
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
    public LongToIntegerMap(final String name, final long maximumSize, final int hashSize,
            final int keyMemoryBlockSize, final int keySubArraySize, final int valueMemoryBlockSize,
            final int valueSubArraySize)
    {
        super(name, maximumSize, hashSize, keyMemoryBlockSize, keySubArraySize,
                valueMemoryBlockSize, valueSubArraySize);
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

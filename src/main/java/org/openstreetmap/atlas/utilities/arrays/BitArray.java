package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} for type {@link Boolean}
 *
 * @author matthieun
 */
public class BitArray extends LargeArray<Boolean>
{
    /**
     * {@link PrimitiveArray} for type {@link Boolean}
     *
     * @author matthieun
     */
    public static class BitPrimitiveArray extends PrimitiveArray<Boolean>
    {
        private static final long serialVersionUID = 5686729947785133814L;
        private final boolean[] array = new boolean[size()];

        public BitPrimitiveArray(final int size)
        {
            super(size);
        }

        @Override
        public Boolean get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<Boolean> getNewArray(final int size)
        {
            return new BitPrimitiveArray(size);
        }

        @Override
        public void set(final int index, final Boolean item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = -342493023854989301L;

    public BitArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public BitArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<Boolean> getNewArray(final int size)
    {
        return new BitPrimitiveArray(size);
    }
}

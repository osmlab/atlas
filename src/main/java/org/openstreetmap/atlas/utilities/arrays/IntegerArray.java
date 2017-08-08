package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} for type {@link Integer}
 *
 * @author matthieun
 */
public class IntegerArray extends LargeArray<Integer>
{
    /**
     * {@link PrimitiveArray} for type {@link Integer}
     *
     * @author matthieun
     */
    public static class IntegerPrimitiveArray extends PrimitiveArray<Integer>
    {
        private static final long serialVersionUID = -3932998946137654750L;
        private final int[] array = new int[size()];

        public IntegerPrimitiveArray(final int size)
        {
            super(size);
        }

        @Override
        public Integer get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<Integer> getNewArray(final int size)
        {
            return new IntegerPrimitiveArray(size);
        }

        @Override
        public void set(final int index, final Integer item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = -6015640040842668449L;

    public IntegerArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public IntegerArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<Integer> getNewArray(final int size)
    {
        return new IntegerPrimitiveArray(size);
    }
}

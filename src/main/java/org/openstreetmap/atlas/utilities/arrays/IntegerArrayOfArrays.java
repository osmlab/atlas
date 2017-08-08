package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} of arrays of int (int[])
 *
 * @author matthieun
 */
public class IntegerArrayOfArrays extends LargeArray<int[]>
{
    /**
     * {@link PrimitiveArray} for type {@link Long}
     *
     * @author matthieun
     */
    public static class PrimitiveIntegerArrayArray extends PrimitiveArray<int[]>
    {
        private static final long serialVersionUID = -4397674333836117261L;
        private final int[][] array = new int[size()][];

        public PrimitiveIntegerArrayArray(final int size)
        {
            super(size);
        }

        @Override
        public int[] get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<int[]> getNewArray(final int size)
        {
            return new PrimitiveIntegerArrayArray(size);
        }

        @Override
        public void set(final int index, final int[] item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = 6793499910834785994L;

    public IntegerArrayOfArrays(final long maximumSize)
    {
        super(maximumSize);
    }

    public IntegerArrayOfArrays(final long maximumSize, final int memoryBlockSize,
            final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<int[]> getNewArray(final int size)
    {
        return new PrimitiveIntegerArrayArray(size);
    }
}

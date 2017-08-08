package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} for type {@link Boolean}
 *
 * @author matthieun
 */
public class BooleanArray extends LargeArray<Boolean>
{
    /**
     * {@link PrimitiveArray} for type {@link Boolean}
     *
     * @author matthieun
     */
    public static class BooleanPrimitiveArray extends PrimitiveArray<Boolean>
    {
        private static final long serialVersionUID = -3932998946137654750L;
        private final boolean[] array = new boolean[size()];

        public BooleanPrimitiveArray(final int size)
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
            return new BooleanPrimitiveArray(size);
        }

        @Override
        public void set(final int index, final Boolean item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = -6015640040842668449L;

    public BooleanArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public BooleanArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<Boolean> getNewArray(final int size)
    {
        return new BooleanPrimitiveArray(size);
    }
}

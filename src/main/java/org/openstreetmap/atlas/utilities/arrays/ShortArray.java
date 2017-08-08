package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} for type {@link Short}
 *
 * @author matthieun
 */
public class ShortArray extends LargeArray<Short>
{
    /**
     * {@link PrimitiveArray} for type {@link Short}
     *
     * @author matthieun
     */
    public static class ShortPrimitiveArray extends PrimitiveArray<Short>
    {
        private static final long serialVersionUID = 3177690048477030833L;
        private final short[] array = new short[size()];

        public ShortPrimitiveArray(final int size)
        {
            super(size);
        }

        @Override
        public Short get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<Short> getNewArray(final int size)
        {
            return new ShortPrimitiveArray(size);
        }

        @Override
        public void set(final int index, final Short item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = 6867216948199207925L;

    public ShortArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public ShortArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<Short> getNewArray(final int size)
    {
        return new ShortPrimitiveArray(size);
    }
}

package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} for type {@link Long}
 *
 * @author matthieun
 */
public class LongArray extends LargeArray<Long>
{
    /**
     * {@link PrimitiveArray} for type {@link Long}
     *
     * @author matthieun
     */
    public static class PrimitiveLongArray extends PrimitiveArray<Long>
    {
        private static final long serialVersionUID = 8325024756132919495L;
        private final long[] array = new long[size()];

        public PrimitiveLongArray(final int size)
        {
            super(size);
        }

        @Override
        public Long get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<Long> getNewArray(final int size)
        {
            return new PrimitiveLongArray(size);
        }

        @Override
        public void set(final int index, final Long item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = -6368556371326217582L;

    public LongArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public LongArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<Long> getNewArray(final int size)
    {
        return new PrimitiveLongArray(size);
    }
}

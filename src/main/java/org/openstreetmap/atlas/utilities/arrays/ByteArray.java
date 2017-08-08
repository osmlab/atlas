package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} for type {@link Byte}
 *
 * @author matthieun
 */
public class ByteArray extends LargeArray<Byte>
{
    /**
     * {@link PrimitiveArray} for type {@link Byte}
     *
     * @author matthieun
     */
    public static class BytePrimitiveArray extends PrimitiveArray<Byte>
    {
        private static final long serialVersionUID = 6928093441524751750L;
        private final byte[] array = new byte[size()];

        public BytePrimitiveArray(final int size)
        {
            super(size);
        }

        @Override
        public Byte get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<Byte> getNewArray(final int size)
        {
            return new BytePrimitiveArray(size);
        }

        @Override
        public void set(final int index, final Byte item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = 6401198662134364211L;

    public ByteArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public ByteArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<Byte> getNewArray(final int size)
    {
        return new BytePrimitiveArray(size);
    }
}

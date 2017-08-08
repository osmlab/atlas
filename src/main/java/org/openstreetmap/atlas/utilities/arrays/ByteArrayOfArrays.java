package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} of arrays of byte (byte[])
 *
 * @author matthieun
 */
public class ByteArrayOfArrays extends LargeArray<byte[]>
{
    /**
     * {@link PrimitiveArray} for type {@link Byte}
     *
     * @author matthieun
     */
    public static class PrimitiveByteArrayArray extends PrimitiveArray<byte[]>
    {
        private static final long serialVersionUID = -89500356133572470L;

        private final byte[][] array = new byte[size()][];

        public PrimitiveByteArrayArray(final int size)
        {
            super(size);
        }

        @Override
        public byte[] get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<byte[]> getNewArray(final int size)
        {
            return new PrimitiveByteArrayArray(size);
        }

        @Override
        public void set(final int index, final byte[] item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = 9114627155973700091L;

    public ByteArrayOfArrays(final long maximumSize)
    {
        super(maximumSize);
    }

    public ByteArrayOfArrays(final long maximumSize, final int memoryBlockSize,
            final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<byte[]> getNewArray(final int size)
    {
        return new PrimitiveByteArrayArray(size);
    }
}

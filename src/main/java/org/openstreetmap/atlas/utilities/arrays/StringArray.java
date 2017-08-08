package org.openstreetmap.atlas.utilities.arrays;

/**
 * {@link LargeArray} of Strings
 *
 * @author matthieun
 */
public class StringArray extends LargeArray<String>
{
    /**
     * {@link PrimitiveArray} of Strings.
     *
     * @author matthieun
     */
    public static class StringPrimitiveArray extends PrimitiveArray<String>
    {
        private static final long serialVersionUID = 6050182547243598715L;
        private final String[] array;

        public StringPrimitiveArray(final int size)
        {
            super(size);
            this.array = new String[size];
        }

        @Override
        public String get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<String> getNewArray(final int size)
        {
            return new StringPrimitiveArray(size);
        }

        @Override
        public void set(final int index, final String item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = 5462179570391723788L;

    public StringArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public StringArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<String> getNewArray(final int size)
    {
        return new StringPrimitiveArray(size);
    }
}

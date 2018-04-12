package org.openstreetmap.atlas.utilities.arrays;

import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoIntegerArrayOfArraysAdapter;

/**
 * {@link LargeArray} of arrays of int (int[])
 *
 * @author matthieun
 * @author lcram
 */
public class IntegerArrayOfArrays extends LargeArray<int[]> implements ProtoSerializable
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
    public boolean equals(final Object other)
    {
        if (other instanceof IntegerArrayOfArrays)
        {
            if (this == other)
            {
                return true;
            }
            final IntegerArrayOfArrays that = (IntegerArrayOfArrays) other;
            if (this.size() != that.size())
            {
                return false;
            }
            for (int index = 0; index < this.size(); index++)
            {
                final int[] thisSubArray = this.get(index);
                final int[] thatSubArray = that.get(index);
                if (thisSubArray.length != thatSubArray.length)
                {
                    return false;
                }
                for (int subIndex = 0; subIndex < thisSubArray.length; subIndex++)
                {
                    if (thisSubArray[subIndex] != thatSubArray[subIndex])
                    {
                        return false;
                    }
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoIntegerArrayOfArraysAdapter();
    }

    /*
     * NOTE: This hashCode implementation uses the array object references and not the actual array
     * values. Keep this in mind before using.
     */
    @Override
    public int hashCode()
    {
        final int initialPrime = 31;
        final int hashSeed = 37;

        int hash = initialPrime;
        for (int index = 0; index < this.size(); index++)
        {
            hash = hashSeed * hash + this.get(index).hashCode();
        }

        return hash;
    }

    @Override
    protected PrimitiveArray<int[]> getNewArray(final int size)
    {
        return new PrimitiveIntegerArrayArray(size);
    }
}

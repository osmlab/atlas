package org.openstreetmap.atlas.utilities.arrays;

import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoLongArrayOfArraysAdapter;

/**
 * {@link LargeArray} of arrays of long (long[])
 *
 * @author matthieun
 * @author lcram
 */
public class LongArrayOfArrays extends LargeArray<long[]> implements ProtoSerializable
{
    /**
     * {@link PrimitiveArray} for type {@link Long}
     *
     * @author matthieun
     */
    public static class PrimitiveLongArrayArray extends PrimitiveArray<long[]>
    {
        private static final long serialVersionUID = 8377199081867533638L;
        private final long[][] array = new long[size()][];

        public PrimitiveLongArrayArray(final int size)
        {
            super(size);
        }

        @Override
        public long[] get(final int index)
        {
            return this.array[index];
        }

        @Override
        public PrimitiveArray<long[]> getNewArray(final int size)
        {
            return new PrimitiveLongArrayArray(size);
        }

        @Override
        public void set(final int index, final long[] item)
        {
            this.array[index] = item;
        }
    }

    private static final long serialVersionUID = -1847060797258075365L;

    public LongArrayOfArrays(final long maximumSize)
    {
        super(maximumSize);
    }

    public LongArrayOfArrays(final long maximumSize, final int memoryBlockSize,
            final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoLongArrayOfArraysAdapter();
    }

    @Override
    protected PrimitiveArray<long[]> getNewArray(final int size)
    {
        return new PrimitiveLongArrayArray(size);
    }
}

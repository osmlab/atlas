package org.openstreetmap.atlas.utilities.arrays;

import java.util.Objects;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasSerializer;
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

    /**
     * This nullary constructor is solely for use by the {@link PackedAtlasSerializer}, which calls
     * it using reflection. It allows the serializer code to obtain a handle on a
     * {@link LongArrayOfArrays} that it can use to grab the correct {@link ProtoAdapter}. The
     * object initialized with this constructor will be corrupted for general use and should be
     * discarded.
     */
    @SuppressWarnings("unused")
    private LongArrayOfArrays()
    {
        super();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof LongArrayOfArrays)
        {
            if (this == other)
            {
                return true;
            }
            final LongArrayOfArrays that = (LongArrayOfArrays) other;
            if (!Objects.equals(this.getName(), that.getName()))
            {
                return false;
            }
            if (this.size() != that.size())
            {
                return false;
            }
            for (long index = 0; index < this.size(); index++)
            {
                final long[] thisSubArray = this.get(index);
                final long[] thatSubArray = that.get(index);
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
        return new ProtoLongArrayOfArraysAdapter();
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

        final int nameHash = this.getName() == null ? 0 : this.getName().hashCode();
        int hash = hashSeed * initialPrime + nameHash;
        hash = hashSeed * hash + Long.valueOf(this.size()).hashCode();
        for (long index = 0; index < this.size(); index++)
        {
            hash = hashSeed * hash + this.get(index).hashCode();
        }

        return hash;
    }

    @Override
    protected PrimitiveArray<long[]> getNewArray(final int size)
    {
        return new PrimitiveLongArrayArray(size);
    }
}

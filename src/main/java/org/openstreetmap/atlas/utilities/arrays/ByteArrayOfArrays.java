package org.openstreetmap.atlas.utilities.arrays;

import java.util.Objects;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasSerializer;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoByteArrayOfArraysAdapter;

/**
 * {@link LargeArray} of arrays of byte (byte[])
 *
 * @author matthieun
 * @author lcram
 */
public class ByteArrayOfArrays extends LargeArray<byte[]> implements ProtoSerializable
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

    /**
     * This nullary constructor is solely for use by the {@link PackedAtlasSerializer}, which calls
     * it using reflection. It allows the serializer code to obtain a handle on a
     * {@link ByteArrayOfArrays} that it can use to grab the correct {@link ProtoAdapter}. The
     * object initialized with this constructor will be corrupted for general use and should be
     * discarded.
     */
    @SuppressWarnings("unused")
    private ByteArrayOfArrays()
    {
        super();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof ByteArrayOfArrays)
        {
            if (this == other)
            {
                return true;
            }
            final ByteArrayOfArrays that = (ByteArrayOfArrays) other;
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
                final byte[] thisSubArray = this.get(index);
                final byte[] thatSubArray = that.get(index);
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
        return new ProtoByteArrayOfArraysAdapter();
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
    protected PrimitiveArray<byte[]> getNewArray(final int size)
    {
        return new PrimitiveByteArrayArray(size);
    }
}

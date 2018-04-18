package org.openstreetmap.atlas.utilities.arrays;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasSerializer;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoLongArrayAdapter;

/**
 * {@link LargeArray} for type {@link Long}
 *
 * @author matthieun
 */
public class LongArray extends LargeArray<Long> implements ProtoSerializable
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

    /**
     * This nullary constructor is solely for use by the {@link PackedAtlasSerializer}, which calls
     * it using reflection. It allows the serializer code to obtain a handle on a {@link LongArray}
     * that it can use to grab the correct {@link ProtoAdapter}. The object initialized with this
     * constructor will be corrupted for general use and should be discarded.
     */
    @SuppressWarnings("unused")
    private LongArray()
    {
        super();
    }

    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoLongArrayAdapter();
    }

    @Override
    protected PrimitiveArray<Long> getNewArray(final int size)
    {
        return new PrimitiveLongArray(size);
    }
}

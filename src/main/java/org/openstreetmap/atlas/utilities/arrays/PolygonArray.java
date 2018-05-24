package org.openstreetmap.atlas.utilities.arrays;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.StringCompressedPolygon;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasSerializer;
import org.openstreetmap.atlas.proto.ProtoSerializable;
import org.openstreetmap.atlas.proto.adapters.ProtoAdapter;
import org.openstreetmap.atlas.proto.adapters.ProtoPolygonArrayAdapter;
import org.openstreetmap.atlas.utilities.arrays.PolyLineArray.PrimitivePointsArray;

/**
 * {@link LargeArray} of {@link StringCompressedPolygon}s, with an interface of {@link Polygon}s.
 *
 * @author matthieun
 */
public class PolygonArray extends LargeArray<Polygon> implements ProtoSerializable
{
    /**
     * Primitive array for polygons
     *
     * @author matthieun
     */
    public static class PrimitivePolygonArray extends PrimitivePointsArray<Polygon>
    {
        private static final long serialVersionUID = 1115133908622542632L;

        public PrimitivePolygonArray(final int size)
        {
            super(size);
        }

        @Override
        public Polygon get(final int index)
        {
            return new StringCompressedPolygon(getEncodings()[index]).asPolygon();
        }

        @Override
        public PrimitiveArray<Polygon> getNewArray(final int size)
        {
            return new PrimitivePolygonArray(size);
        }
    }

    private static final long serialVersionUID = -2337695414673604456L;

    public PolygonArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public PolygonArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    /**
     * This nullary constructor is solely for use by the {@link PackedAtlasSerializer}, which calls
     * it using reflection. It allows the serializer code to obtain a handle on a
     * {@link PolygonArray} that it can use to grab the correct {@link ProtoAdapter}. The object
     * initialized with this constructor will be corrupted for general use and should be discarded.
     */
    @SuppressWarnings("unused")
    private PolygonArray()
    {
        super();
    }

    @Override
    public ProtoAdapter getProtoAdapter()
    {
        return new ProtoPolygonArrayAdapter();
    }

    @Override
    protected PrimitiveArray<Polygon> getNewArray(final int size)
    {
        return new PrimitivePolygonArray(size);
    }
}

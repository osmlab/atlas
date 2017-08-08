package org.openstreetmap.atlas.utilities.arrays;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.StringCompressedPolygon;
import org.openstreetmap.atlas.utilities.arrays.PolyLineArray.PrimitivePointsArray;

/**
 * {@link LargeArray} of {@link StringCompressedPolygon}s, with an interface of {@link Polygon}s.
 *
 * @author matthieun
 */
public class PolygonArray extends LargeArray<Polygon>
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

    @Override
    protected PrimitiveArray<Polygon> getNewArray(final int size)
    {
        return new PrimitivePolygonArray(size);
    }
}

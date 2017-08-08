package org.openstreetmap.atlas.utilities.arrays;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.StringCompressedPolyLine;
import org.openstreetmap.atlas.geography.StringCompressedPolyLine.PolyLineCompressionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link LargeArray} of {@link StringCompressedPolyLine}s, with an interface using {@link PolyLine}
 * s.
 *
 * @author matthieun
 */
public class PolyLineArray extends LargeArray<PolyLine>
{
    /**
     * @author matthieun
     * @param <Poly>
     *            An implementation of {@link PolyLine}
     */
    public abstract static class PrimitivePointsArray<Poly extends PolyLine>
            extends PrimitiveArray<Poly>
    {
        private static final long serialVersionUID = 3532399057462343784L;

        private final byte[][] encodings;

        public PrimitivePointsArray(final int size)
        {
            super(size);
            this.encodings = new byte[size][];
        }

        @Override
        public void set(final int index, final Poly item)
        {
            // Here, whether it is a polyline or a polygon does not matter, the encodings will be
            // the same
            StringCompressedPolyLine compressed;
            try
            {
                compressed = new StringCompressedPolyLine(item);
            }
            catch (final PolyLineCompressionException e)
            {
                logger.error("Unable to compress polyLine {} at index {}. Sending to Null Island.",
                        item, index, e);
                compressed = new StringCompressedPolyLine(new PolyLine(Location.CENTER));
            }
            this.encodings[index] = compressed.getEncoding();
        }

        protected byte[][] getEncodings()
        {
            return this.encodings;
        }
    }

    /**
     * A primitive array specifically for {@link PolyLine}
     *
     * @author matthieun
     */
    public static class PrimitivePolyLineArray extends PrimitivePointsArray<PolyLine>
    {
        private static final long serialVersionUID = -9008848366079793820L;

        public PrimitivePolyLineArray(final int size)
        {
            super(size);
        }

        @Override
        public PolyLine get(final int index)
        {
            return new StringCompressedPolyLine(getEncodings()[index]).asPolyLine();
        }

        @Override
        public PrimitiveArray<PolyLine> getNewArray(final int size)
        {
            return new PrimitivePolyLineArray(size);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(PolyLineArray.class);

    private static final long serialVersionUID = -4475168018638543482L;

    public PolyLineArray(final long maximumSize)
    {
        super(maximumSize);
    }

    public PolyLineArray(final long maximumSize, final int memoryBlockSize, final int subArraySize)
    {
        super(maximumSize, memoryBlockSize, subArraySize);
    }

    @Override
    protected PrimitiveArray<PolyLine> getNewArray(final int size)
    {
        return new PrimitivePolyLineArray(size);
    }
}

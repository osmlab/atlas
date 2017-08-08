package org.openstreetmap.atlas.geography;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PolyLine} that is compressed using delta encoding. This is efficient when the
 * {@link PolyLine} has a lot of points close by.
 *
 * @author matthieun
 */
public class CompressedPolyLine implements Located, Serializable
{
    /**
     * @author matthieun
     */
    private static class ByteSign
    {
        private final byte[] bytes;
        private final boolean sign;

        ByteSign(final byte[] bytes, final boolean sign)
        {
            this.bytes = bytes;
            this.sign = sign;
        }

        public byte[] getBytes()
        {
            return this.bytes;
        }

        public boolean isSign()
        {
            return this.sign;
        }
    }

    private static final long serialVersionUID = -7813027521625470225L;

    private static final int BYTE_FULL_MASK = 0xFF;
    private static final int BYTE_SIZE = 8;
    private static final int INT_SIGN_MASK = 0x80000000;
    private static final int INT_NO_SIGN_MASK = 0x7FFFFFFF;

    private final byte[][] positions;

    private final boolean[] signs;

    public CompressedPolyLine(final byte[][] positions, final boolean[] signs)
    {
        this.positions = positions;
        this.signs = signs;
    }

    /**
     * Create a compressed version of a {@link PolyLine}
     *
     * @param polyLine
     *            The {@link PolyLine} to compress.
     */
    public CompressedPolyLine(final PolyLine polyLine)
    {
        final List<byte[]> positions = new ArrayList<>();
        final List<Boolean> signs = new ArrayList<>();
        int formerLatitude = 0;
        int formerLongitude = 0;
        for (final Location location : polyLine)
        {
            final int latitude = (int) location.getLatitude().asDm7();
            final int longitude = (int) location.getLongitude().asDm7();
            final int deltaLatitude = latitude - formerLatitude;
            final int deltaLongitude = longitude - formerLongitude;
            formerLatitude = latitude;
            formerLongitude = longitude;
            final ByteSign latShrink = shrink(deltaLatitude);
            final ByteSign lonShrink = shrink(deltaLongitude);
            positions.add(latShrink.getBytes());
            signs.add(latShrink.isSign());
            positions.add(lonShrink.getBytes());
            signs.add(lonShrink.isSign());
        }
        this.positions = new byte[positions.size()][];
        for (int i = 0; i < positions.size(); i++)
        {
            this.positions[i] = positions.get(i);
        }
        this.signs = new boolean[signs.size()];
        for (int i = 0; i < signs.size(); i++)
        {
            this.signs[i] = signs.get(i);
        }
    }

    /**
     * @return An expanded {@link PolyLine}
     */
    public PolyLine asPolyLine()
    {
        boolean lat = true;
        int latitude = 0;
        int longitude = 0;
        final List<Location> locations = new ArrayList<>();
        for (int index = 0; index < this.positions.length; index++)
        {
            final byte[] result = this.positions[index];
            if (lat)
            {
                latitude += expand(result, index);
                lat = false;
            }
            else
            {
                longitude += expand(result, index);
                locations.add(new Location(Latitude.dm7(latitude), Longitude.dm7(longitude)));
                lat = true;
            }
        }
        return new PolyLine(locations);
    }

    @Override
    public Rectangle bounds()
    {
        return asPolyLine().bounds();
    }

    public byte[][] getPositions()
    {
        return this.positions;
    }

    public boolean[] getSigns()
    {
        return this.signs;
    }

    @Override
    public String toString()
    {
        return asPolyLine().toString();
    }

    /**
     * Transform an array of bytes into an int. If the bytes are 0x4A and 0x0F, with a negative sign
     * (from the index in the signs array) the returned int will be 0x80000F4A.
     *
     * @param result
     *            The shrunk value
     * @param index
     *            The index of the sign
     * @return The expanded value
     */
    private int expand(final byte[] result, final int index)
    {
        int placeholder = 0;
        for (int i = 0; i < result.length; i++)
        {
            final byte byteValue = result[i];
            placeholder |= byteValue & BYTE_FULL_MASK;
            if (i < result.length - 1)
            {
                placeholder <<= BYTE_SIZE;
            }
        }
        final boolean negative = this.signs[index];
        if (negative)
        {
            placeholder |= INT_SIGN_MASK;
        }
        return placeholder;
    }

    /**
     * Browse the value, byte after byte, and keep only the bytes that have significance. So an int
     * 0x80000F4A will return an array of two bytes, 0x4A and 0x0F, and a negative sign. All the 0
     * bytes will be thrown out.
     *
     * @param value
     *            The value to shrink
     * @return The shrunk value.
     */
    private ByteSign shrink(final int value)
    {
        // Get rid of the sign
        int placeholder = value & INT_NO_SIGN_MASK;
        final List<Byte> bytes = new ArrayList<>();
        while (Math.abs(placeholder) > 0)
        {
            final byte byteValue = (byte) placeholder;
            bytes.add(byteValue);
            placeholder >>>= BYTE_SIZE;
        }
        final int size = bytes.size();
        final byte[] result = new byte[size];
        for (int i = 0; i < size; i++)
        {
            result[i] = bytes.get(size - 1 - i);
        }
        return new ByteSign(result, value < 0);
    }
}

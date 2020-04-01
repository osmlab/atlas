package org.openstreetmap.atlas.geography;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encode a {@link PolyLine} using an algorithm derived from the MapQuest variant of the Google
 * Polyline Encoding Format. The encoding scheme falls back on WKB in edge cases when the algorithm
 * fails (such as when two consecutive points in the polyline have a longitude difference of greater
 * than 180 degrees).
 *
 * @see <a href="https://developer.mapquest.com/documentation/common/encode-decode">The MapQuest
 *      algorithm</a>
 * @see <a href=
 *      "https://developers.google.com/maps/documentation/utilities/polylinealgorithm?csw=1">Google
 *      Polyline Encoding Format</a>
 * @author matthieun
 */
public class StringCompressedPolyLine implements Serializable
{
    /**
     * @author matthieun
     */
    public static class PolyLineCompressionException extends CoreException
    {
        private static final long serialVersionUID = -3974024747280370420L;

        public PolyLineCompressionException(final String message, final Object... items)
        {
            super(message, items);
        }
    }

    private static final long serialVersionUID = 5315700936842774861L;

    // dm7
    private static final int PRECISION = 7;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int ENCODING_OFFSET_MINUS_ONE = 63;
    private static final int FIVE_BIT_MASK = 0x1f;
    private static final int SIXTH_BIT_MASK = 0x20;
    private static final int BIT_SHIFT = 5;
    // To allow for degree of magnitude 7 precision, two longitudes should not be too far apart.
    // Using 180 degrees as a limit.
    private static final long MAXIMUM_DELTA_LONGITUDE_IN_DEGREES = 180;
    private static final long MAXIMUM_DELTA_LONGITUDE = (long) (MAXIMUM_DELTA_LONGITUDE_IN_DEGREES
            * Math.pow(10, PRECISION));

    /*
     * If the first byte of the encoding array is this sentinel value, then the following encoding
     * is WKB and not string-compressed. We use '0' as the sentinel value since the string
     * compression algorithm will always use printable ASCII characters. There will never be a 0
     * byte in a valid string-compressed polyline.
     */
    private static final byte WKB_SENTINEL = 0;

    private static final Logger logger = LoggerFactory.getLogger(StringCompressedPolyLine.class);

    private byte[] encoding;

    public StringCompressedPolyLine(final byte[] encoding)
    {
        this.encoding = encoding;
    }

    public StringCompressedPolyLine(final PolyLine polyLine)
    {
        try
        {
            this.encoding = compress(polyLine, PRECISION).getBytes(CHARSET);
        }
        catch (final PolyLineCompressionException exception)
        {
            logger.warn("Unable to use string compression", exception);
            this.encoding = getWkbFallback(polyLine);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Could not compress polyline.", exception);
        }
    }

    public PolyLine asPolyLine()
    {
        if (this.encoding[0] == WKB_SENTINEL)
        {
            final byte[] strippedEncoding = new byte[this.encoding.length - 1];
            System.arraycopy(this.encoding, 1, strippedEncoding, 0, strippedEncoding.length);
            return PolyLine.wkb(strippedEncoding);
        }
        else
        {
            String encodedString = null;
            try
            {
                encodedString = new String(this.encoding, CHARSET);
                return asPolyLine(encodedString, PRECISION);
            }
            catch (final Exception exception)
            {
                throw new CoreException(
                        "Could not decompress polyline:\nEncoding: \'{}\'\nString: \'\'.",
                        this.encoding, encodedString, exception);
            }
        }
    }

    public byte[] getEncoding()
    {
        return this.encoding;
    }

    @Override
    public String toString()
    {
        if (this.encoding[0] == WKB_SENTINEL)
        {
            final byte[] strippedEncoding = new byte[this.encoding.length - 1];
            System.arraycopy(this.encoding, 1, strippedEncoding, 0, strippedEncoding.length);
            return PolyLine.wkb(strippedEncoding).toWkt();
        }
        else
        {
            try
            {
                return new String(this.encoding, CHARSET);
            }
            catch (final Exception e)
            {
                throw new CoreException("Could not stringify byte array.", e);
            }
        }
    }

    private PolyLine asPolyLine(final String encoded, final int precision)
    {
        final double precision2 = Math.pow(10, -precision);
        final int length = encoded.length();
        int index = 0;
        int latitude = 0;
        int longitude = 0;
        final List<Location> array = new ArrayList<>();
        while (index < length)
        {
            int byteEncoded;
            int shift = 0;
            int result = 0;
            do
            {
                byteEncoded = Character.codePointAt(encoded, index++) - ENCODING_OFFSET_MINUS_ONE;
                result |= (byteEncoded & FIVE_BIT_MASK) << shift;
                shift += BIT_SHIFT;
            }
            while (byteEncoded >= SIXTH_BIT_MASK);
            final int deltaLatitude = (result & 1) > 0 ? ~(result >>> 1) : result >>> 1;
            latitude += deltaLatitude;
            shift = 0;
            result = 0;
            do
            {
                byteEncoded = Character.codePointAt(encoded, index++) - ENCODING_OFFSET_MINUS_ONE;
                result |= (byteEncoded & FIVE_BIT_MASK) << shift;
                shift += BIT_SHIFT;
            }
            while (byteEncoded >= SIXTH_BIT_MASK);
            final int deltalongitude = (result & 1) > 0 ? ~(result >>> 1) : result >>> 1;
            longitude += deltalongitude;
            array.add(new Location(Latitude.degrees(latitude * precision2),
                    Longitude.degrees(longitude * precision2)));
        }
        return new PolyLine(array);
    }

    private String compress(final PolyLine points, final int precision0)
    {
        long oldLatitude = 0;
        long oldLongitude = 0;
        final StringBuilder encoded = new StringBuilder();
        final double precision = Math.pow(10, precision0);
        Location last = Location.CENTER;
        for (final Location location : points)
        {
            // Round to N decimal places
            final long latitude = Math.round(location.getLatitude().asDegrees() * precision);
            final long longitude = Math.round(location.getLongitude().asDegrees() * precision);

            // Encode the differences between the points
            encoded.append(encodeNumber(latitude - oldLatitude));
            final long deltaLongitude = longitude - oldLongitude;
            if (Math.abs(deltaLongitude) > MAXIMUM_DELTA_LONGITUDE)
            {
                throw new PolyLineCompressionException(
                        "Unable to compress the polyLine, two consecutive points ({} and {}) are too far apart in longitude: {} degrees.",
                        last, location, deltaLongitude / precision);
            }
            encoded.append(encodeNumber(deltaLongitude));

            oldLatitude = latitude;
            oldLongitude = longitude;
            last = location;
        }
        return encoded.toString();
    }

    private String encodeNumber(final long number0)
    {
        long number = number0 << 1;
        if (number < 0)
        {
            number = ~number;
        }
        final StringBuilder encoded = new StringBuilder();
        while (number >= SIXTH_BIT_MASK)
        {
            encoded.append(String.valueOf(Character.toChars(
                    (SIXTH_BIT_MASK | (int) number & FIVE_BIT_MASK) + ENCODING_OFFSET_MINUS_ONE)));
            number >>>= BIT_SHIFT;
        }
        encoded.append(String.valueOf(Character.toChars((int) number + ENCODING_OFFSET_MINUS_ONE)));
        return encoded.toString();
    }

    private byte[] getWkbFallback(final PolyLine polyLine)
    {
        final byte[] wkbEncoding = polyLine.toWkb();
        final byte[] finalEncoding = new byte[1 + wkbEncoding.length];
        finalEncoding[0] = WKB_SENTINEL;
        System.arraycopy(wkbEncoding, 0, finalEncoding, 1, wkbEncoding.length);
        return finalEncoding;
    }
}

package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.StringCompressedPolyLine.PolyLineCompressionException;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Test a {@link StringCompressedPolyLine}
 *
 * @author matthieun
 */
public class StringCompressedPolyLineTest
{
    @Test
    public void testCompressionDecompression()
    {
        final PolyLine polyLine = PolyLine.TEST_POLYLINE;
        System.out.println(polyLine);
        final StringCompressedPolyLine compressed = new StringCompressedPolyLine(polyLine);
        System.out.println(compressed);
        final PolyLine decompressed = compressed.asPolyLine();
        System.out.println(decompressed);
        Assert.assertEquals(polyLine, decompressed);
    }

    @Test
    public void testCompressionError1()
    {
        final Location location1 = new Location(Latitude.degrees(0.0), Longitude.degrees(-179.0));
        final Location location2 = new Location(Latitude.degrees(0.0), Longitude.degrees(179.0));
        final PolyLine line = new PolyLine(location1, location2);
        final StringCompressedPolyLine compressedLine = new StringCompressedPolyLine(line);
    }

    @Test
    public void testCompressionError2()
    {
        final Location location1 = new Location(Latitude.degrees(0.0), Longitude.degrees(179.0));
        final Location location2 = new Location(Latitude.degrees(0.0), Longitude.degrees(-179.0));
        final PolyLine line = new PolyLine(location1, location2);
        final StringCompressedPolyLine compressedLine = new StringCompressedPolyLine(line);
    }

    /**
     * Here the delta longitude between loc2 and loc3 is more than 180 degrees, hence the
     * compression exception.
     */
    @Test(expected = PolyLineCompressionException.class)
    public void testJumpyPolyLine()
    {
        final Location loc1 = Location.forString("5.972761,-75.8373644");
        final Location loc2 = Location.forString("5.9712693,-75.8363875");
        final Location loc3 = Location.forString("18.6398595,157.7635783");
        final Location loc4 = Location.forString("15.3405183,13.9936102");
        final PolyLine polyLine = new PolyLine(loc1, loc2, loc3, loc4);

        System.out.println(polyLine);
        new StringCompressedPolyLine(polyLine);
    }

    @Test
    public void testSmallPolyLine()
    {
        final Distance distance = Distance.meters(100);
        final Location loc1 = Location.TEST_5;
        final Location loc2 = loc1.shiftAlongGreatCircle(Heading.EAST, distance);
        final Location loc3 = loc2.shiftAlongGreatCircle(Heading.NORTH, distance);
        final Location loc4 = loc3.shiftAlongGreatCircle(Heading.WEST, distance);
        final Location loc5 = loc4.shiftAlongGreatCircle(Heading.NORTH, distance);
        final PolyLine polyLine = new PolyLine(loc1, loc2, loc3, loc4, loc5);

        System.out.println(polyLine);
        final StringCompressedPolyLine compressed = new StringCompressedPolyLine(polyLine);
        System.out.println(compressed);
        final PolyLine decompressed = compressed.asPolyLine();
        System.out.println(decompressed);
        Assert.assertEquals(polyLine, decompressed);
    }
}

package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;
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

    /**
     * Here the delta longitude between loc1/loc2 and loc3/loc4 is more than 180 degrees, so the
     * algorithm falls back on WKB.
     */
    @Test
    public void testCompressionWkbFallback()
    {
        final Location location1 = new Location(Latitude.degrees(45.0), Longitude.degrees(-179.0));
        final Location location2 = new Location(Latitude.degrees(45.0), Longitude.degrees(179.0));
        final Location location3 = new Location(Latitude.degrees(45.0), Longitude.degrees(179.0));
        final Location location4 = new Location(Latitude.degrees(45.0), Longitude.degrees(-179.0));
        final PolyLine line1 = new PolyLine(location1, location2);
        final PolyLine line2 = new PolyLine(location3, location4);
        final StringCompressedPolyLine compressedLine1 = new StringCompressedPolyLine(line1);
        final StringCompressedPolyLine compressedLine2 = new StringCompressedPolyLine(line2);

        // the toString method should return WKT since the compression is WKB instead of MapQuest
        // string compression
        Assert.assertEquals(compressedLine1.toString(), compressedLine1.asPolyLine().toWkt());
        Assert.assertEquals(compressedLine2.toString(), compressedLine2.asPolyLine().toWkt());
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

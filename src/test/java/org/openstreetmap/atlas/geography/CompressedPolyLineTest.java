package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test a {@link CompressedPolyLine}
 *
 * @author matthieun
 */
public class CompressedPolyLineTest
{
    @Test
    public void testCompressionDecompression()
    {
        final PolyLine polyLine = PolyLine.TEST_POLYLINE;
        System.out.println(polyLine);
        final CompressedPolyLine compressed = new CompressedPolyLine(polyLine);
        System.out.println(compressed);
        final PolyLine decompressed = compressed.asPolyLine();
        System.out.println(decompressed);
        Assert.assertEquals(polyLine, decompressed);
    }
}

package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

/**
 * Test conversion between Well Known Binary (WKB) and {@link PolyLine}.
 *
 * @author robert_stack
 * @author mgostintsev
 */
public class WkbPolyLineConverterTest
{
    private static final Logger logger = LoggerFactory.getLogger(WkbPolyLineConverterTest.class);

    @Test
    public void testSinglePointConversion()
    {
        final Location location = Location.TEST_1;
        final PolyLine polyline = new PolyLine(location);
        final byte[] wkb = new WkbPolyLineConverter().convert(polyline);
        final PolyLine polylineConverted = new WkbPolyLineConverter().backwardConvert(wkb);
        Assert.assertTrue("Input/output PolyLine must be the same",
                polyline.equals(polylineConverted));
    }

    @Test
    public void testWkbConversions()
    {
        final List<PolyLine> polyLines = new ArrayList<>();
        final List<String> strings = new ArrayList<>();

        polyLines.add(new PolyLine(Location.TEST_6, Location.TEST_4, Location.TEST_1));
        strings.add(
                "000000000200000003C05E822C343B70EF4042A9A8049667B6C05E81DA059A73B44042AA8DC11E42E1C05E809CBAB649D44042AAEB702602C9");
        polyLines.add(new Segment(Location.TEST_2, Location.TEST_5));
        strings.add(
                "000000000200000002C05E81D25AAB47414042A92B1B36BD2BC05E81FC04C8BC9D4042B1FD0D0678C0");

        final WkbPolyLineConverter converter = new WkbPolyLineConverter();

        // Forward convert from PolyLine to WKB and verify against prebuilt WKB
        int stringIndex = 0;
        for (final PolyLine polyLine : polyLines)
        {
            final byte[] convertedWkb = converter.convert(polyLine);
            final String convertedWkbString = WKBWriter.toHex(convertedWkb);
            Assert.assertEquals(convertedWkbString, strings.get(stringIndex));
            logger.trace(convertedWkbString);
            reportWkt(convertedWkb);
            stringIndex++;
        }

        stringIndex = 0;
        // Backward convert from prebuilt WKB to PolyLine and verify against known Polyline
        for (final String string : strings)
        {
            final byte[] wkb = WKBReader.hexToBytes(string);
            final PolyLine convertedPolyLine = converter.backwardConvert(wkb);
            Assert.assertEquals(convertedPolyLine, polyLines.get(stringIndex));
            logger.trace(convertedPolyLine.toString());
            reportWkt(wkb);
            stringIndex++;
        }

    }

    // This just prints out a little more diagnostic information for any audience who is used to
    // viewing location information in Postgres LINESTRING format.
    private void reportWkt(final byte[] edgeWkb)
    {
        try
        {
            Geometry geom = null;
            geom = new WKBReader().read(edgeWkb);
            final String wktString = geom.toText();
            logger.trace(wktString);
        }
        catch (final Exception e)
        {
            logger.error("Fail convert and parse WKB {}", edgeWkb);
            Assert.fail();
        }
    }
}

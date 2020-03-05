package org.openstreetmap.atlas.geography.converters.jts;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.LineString;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;

/**
 * @author lcram
 */
public class JtsPolyLineConverterTest
{
    @Test
    public void testBackwardConvert()
    {
        final PolyLine polyLine = PolyLine.wkt("LINESTRING(0 0, 1 1)");
        final LineString lineString = new JtsPolyLineConverter().convert(polyLine);
        Assert.assertEquals("LINESTRING (0 0, 1 1)",
                new JtsPolyLineConverter().backwardConvert(lineString).toWkt());
    }

    @Test
    public void testConvertLineString()
    {
        final PolyLine polyLine = PolyLine.wkt("LINESTRING(0 0, 1 1)");
        Assert.assertEquals("LINESTRING (0 0, 1 1)",
                new JtsPolyLineConverter().convert(polyLine).toString());
    }

    @Test
    public void testConvertLinearRing()
    {
        final PolyLine polyLine = Polygon.wkt(
                "POLYGON ((-61.875 15.2841851, -61.875 15.9613291, -61.171875 15.9613291, -61.171875 15.2841851, -61.875 15.2841851))");
        Assert.assertEquals(
                "LINEARRING (-61.875 15.2841851, -61.875 15.9613291, -61.171875 15.9613291, -61.171875 15.2841851, -61.875 15.2841851)",
                new JtsPolyLineConverter().convert(polyLine).toString());
    }
}

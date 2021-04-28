package org.openstreetmap.atlas.geography.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;

/**
 * @author Sid
 */
public class WkbPolygonConverterTest
{
    private static final WkbPolygonConverter CONVERTER = new WkbPolygonConverter();

    @Test
    public void testConversion()
    {
        final Polygon polygonA = new Polygon(Location.CROSSING_85_280, Location.CROSSING_85_17,
                Location.TEST_1, Location.TEST_5, Location.CROSSING_85_280);
        final byte[] wkb = CONVERTER.convert(polygonA);
        final Polygon polygonB = CONVERTER.backwardConvert(wkb);
        Assert.assertEquals("Input/output Polygon must be the same", polygonA, polygonB);
    }
}

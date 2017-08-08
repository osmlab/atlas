package org.openstreetmap.atlas.geography.converters;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;

import com.vividsolutions.jts.util.Assert;

/**
 * @author Sid
 */
public class WkBPolygonConverterTest
{
    private WkbPolygonConverter converter;

    @Before
    public void setup()
    {
        this.converter = new WkbPolygonConverter();
    }

    @Test
    public void testConversion()
    {
        final Polygon polygonA = new Polygon(Location.CROSSING_85_280, Location.CROSSING_85_17,
                Location.TEST_1, Location.TEST_5, Location.CROSSING_85_280);
        final byte[] wkb = this.converter.convert(polygonA);
        final Polygon polygonB = this.converter.backwardConvert(wkb);
        Assert.equals(polygonA, polygonB, "Input/output Polygon must be the same");
    }
}

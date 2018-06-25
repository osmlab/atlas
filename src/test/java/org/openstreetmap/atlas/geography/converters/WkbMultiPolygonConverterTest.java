package org.openstreetmap.atlas.geography.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.MultiPolygon;

/**
 * Test for {@link WkbMultiPolygonConverter}
 *
 * @author jklamer
 */
public class WkbMultiPolygonConverterTest
{
    private static WkbMultiPolygonConverter converter = new WkbMultiPolygonConverter();

    @Test
    public void testConversion()
    {
        final MultiPolygon multiPolygonA = MultiPolygon.TEST_MULTI_POLYGON;
        final byte[] wkb = converter.convert(multiPolygonA);
        final MultiPolygon multiPolygonB = converter.backwardConvert(wkb);
        Assert.assertEquals(multiPolygonA, multiPolygonB);
    }
}

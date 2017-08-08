package org.openstreetmap.atlas.geography.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.MultiPolygon;

/**
 * @author matthieun
 */
public class MultiPolygonStringConverterTest
{
    private static final MultiPolygonStringConverter CONVERTER = new MultiPolygonStringConverter();

    @Test
    public void testConversion()
    {
        System.out.println(CONVERTER.backwardConvert(MultiPolygon.TEST_MULTI_POLYGON));
        Assert.assertEquals(MultiPolygon.TEST_MULTI_POLYGON,
                CONVERTER.convert(CONVERTER.backwardConvert(MultiPolygon.TEST_MULTI_POLYGON)));
    }
}

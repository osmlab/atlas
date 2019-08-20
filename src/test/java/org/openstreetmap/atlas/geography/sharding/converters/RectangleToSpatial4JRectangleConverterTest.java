package org.openstreetmap.atlas.geography.sharding.converters;

import org.junit.Assert;
import org.junit.Test;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.impl.RectangleImpl;
import org.openstreetmap.atlas.geography.Rectangle;

/**
 * @author matthieun
 */
public class RectangleToSpatial4JRectangleConverterTest
{
    @Test
    public void testConversion()
    {
        final Rectangle atlasRectangle = Rectangle.TEST_RECTANGLE;
        final org.locationtech.spatial4j.shape.Rectangle spatial4JRectangle = new RectangleImpl(
                -122.031905, -122.029051, 37.328167, 37.330394, SpatialContext.GEO);
        final RectangleToSpatial4JRectangleConverter converter = new RectangleToSpatial4JRectangleConverter();
        Assert.assertEquals(atlasRectangle, converter.convert(spatial4JRectangle));
        Assert.assertEquals(spatial4JRectangle, converter.backwardConvert(atlasRectangle));
    }

    @Test
    public void testLimits()
    {
        final Rectangle atlasRectangle = Rectangle.MAXIMUM;
        final org.locationtech.spatial4j.shape.Rectangle spatial4JRectangle = new RectangleImpl(
                -180.0, 180.0, -90.0, 90.0, SpatialContext.GEO);
        final RectangleToSpatial4JRectangleConverter converter = new RectangleToSpatial4JRectangleConverter();
        Assert.assertEquals(atlasRectangle, converter.convert(spatial4JRectangle));
        Assert.assertEquals(spatial4JRectangle, converter.backwardConvert(atlasRectangle));
    }
}

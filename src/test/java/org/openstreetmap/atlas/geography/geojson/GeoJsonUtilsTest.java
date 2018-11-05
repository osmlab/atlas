package org.openstreetmap.atlas.geography.geojson;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Rectangle;

import com.google.gson.JsonObject;

/**
 * Test functions in GeoJsonUtils
 *
 * @author hallahan
 */
public class GeoJsonUtilsTest
{
    private static final String BOUNDS_POLYGON_GEOJSON = "{\"type\":\"Polygon\",\"coordinates\":[[[-122.031905,37.328167],[-122.031905,37.330394],[-122.029051,37.330394],[-122.029051,37.328167],[-122.031905,37.328167]]]}";

    @Test
    public void testBoundsToPolygonGeometry()
    {
        final Rectangle rectangle = Rectangle.TEST_RECTANGLE;
        final JsonObject polygonGeometry = GeoJsonUtils.boundsToPolygonGeometry(rectangle);
        Assert.assertEquals(BOUNDS_POLYGON_GEOJSON, polygonGeometry.toString());
    }
}

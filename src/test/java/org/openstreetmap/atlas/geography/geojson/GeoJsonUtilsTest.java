package org.openstreetmap.atlas.geography.geojson;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
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
    private static final String GEOMETRY = "{\"type\":\"Point\",\"coordinates\":[-77.7951565,-1.4317173]}";
    private static final String PROPERTIES = "{\"last_edit_user_name\":\"Lover4\",\"last_edit_changeset\":\"35090172\",\"last_edit_time\":\"1446676342000\",\"last_edit_user_id\":\"2789671\",\"iso_country_code\":\"ECU\",\"last_edit_version\":\"2\",\"identifier\":3390274778000000,\"osmIdentifier\":3390274778,\"itemType\":\"NODE\",\"shard\":\"9-144-256\"}";
    private static final String FEATURE = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-77.7951565,-1.4317173]},\"properties\":{\"last_edit_user_name\":\"Lover4\",\"last_edit_changeset\":\"35090172\",\"last_edit_time\":\"1446676342000\",\"last_edit_user_id\":\"2789671\",\"iso_country_code\":\"ECU\",\"last_edit_version\":\"2\",\"identifier\":3390274778000000,\"osmIdentifier\":3390274778,\"itemType\":\"NODE\",\"shard\":\"9-144-256\"}}";

    private static final String BOUNDS_POLYGON_GEOJSON = "{\"type\":\"Polygon\",\"coordinates\":[[[-122.031905,37.328167],[-122.031905,37.330394],[-122.029051,37.330394],[-122.029051,37.328167],[-122.031905,37.328167]]]}";

    private static final double LONGITUDE = -77.7951565;
    private static final double LATITUDE = -1.4317173;
    private static final String COORDINATE = "[-77.7951565,-1.4317173]";
    @Test
    public void testFeature()
    {
        final JsonParser parser = new JsonParser();
        final JsonObject geometry = parser.parse(GEOMETRY).getAsJsonObject();
        final JsonObject properties = parser.parse(PROPERTIES).getAsJsonObject();
        final JsonObject feature = GeoJsonUtils.feature(geometry, properties);
        Assert.assertEquals(FEATURE, feature.toString());
    }

    @Test
    public void testBoundsToPolygonGeometry()
    {
        final Rectangle rectangle = Rectangle.TEST_RECTANGLE;
        final JsonObject polygonGeometry = GeoJsonUtils.boundsToPolygonGeometry(rectangle);
        Assert.assertEquals(BOUNDS_POLYGON_GEOJSON, polygonGeometry.toString());
    }

    @Test
    public void testCoordinate()
    {
        final JsonArray coordinate = GeoJsonUtils.coordinate(LONGITUDE, LATITUDE);
        Assert.assertEquals(COORDINATE, coordinate.toString());
    }
}

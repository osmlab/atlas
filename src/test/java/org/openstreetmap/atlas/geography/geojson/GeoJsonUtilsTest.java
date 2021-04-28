package org.openstreetmap.atlas.geography.geojson;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.items.Edge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    private static final PolyLine EDGE_POLYLINE = PolyLine.wkt(
            "LINESTRING(-122.01024413108826 37.33667453131461,-122.00722932815552 37.33667453131461,-122.00610280036926 37.334942852083415,-122.00563073158263 37.33617976989369,-122.00483679771423 37.33639302952647,-122.00469732284544 37.334959913157256,-122.0042359828949 37.33623095226076)");
    private static final String FEATURE_COLLECTION_STRING = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-122.0102441,37.3366745],[-122.0072293,37.3366745],[-122.0061028,37.3349429],[-122.0056307,37.3361798],[-122.0048368,37.336393],[-122.0046973,37.3349599],[-122.004236,37.336231]]},\"properties\":{\"TheKey\":\"TheValue\",\"identifier\":1,\"osmIdentifier\":0,\"itemType\":\"EDGE\",\"startNode\":0,\"endNode\":1}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-122.004236,37.336231],[-122.0046973,37.3349599],[-122.0048368,37.336393],[-122.0056307,37.3361798],[-122.0061028,37.3349429],[-122.0072293,37.3366745],[-122.0102441,37.3366745]]},\"properties\":{\"TheKey\":\"TheValue\",\"identifier\":-1,\"osmIdentifier\":0,\"itemType\":\"EDGE\",\"startNode\":1,\"endNode\":0}}],\"properties\":{\"aProperty\":\"aValue\"}}";
    private static final String GEOMETRY_COLLECTION_STRING = "{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"LineString\",\"coordinates\":[[-122.0102441,37.3366745],[-122.0072293,37.3366745],[-122.0061028,37.3349429],[-122.0056307,37.3361798],[-122.0048368,37.336393],[-122.0046973,37.3349599],[-122.004236,37.336231]]},{\"type\":\"LineString\",\"coordinates\":[[-122.004236,37.336231],[-122.0046973,37.3349599],[-122.0048368,37.336393],[-122.0056307,37.3361798],[-122.0061028,37.3349429],[-122.0072293,37.3366745],[-122.0102441,37.3366745]]}]}";

    private static final double LONGITUDE = -77.7951565;
    private static final double LATITUDE = -1.4317173;
    private static final String COORDINATE = "[-77.7951565,-1.4317173]";

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
    public void testFeatureCollection()
    {
        final Edge edge = new CompleteEdge(1L, EDGE_POLYLINE,
                Collections.singletonMap("TheKey", "TheValue"), 0L, 1L, Collections.emptySet());
        final Edge reverseEdge = new CompleteEdge(-1L, EDGE_POLYLINE.reversed(),
                Collections.singletonMap("TheKey", "TheValue"), 1L, 0L, Collections.emptySet());

        final JsonObject featureCollection = GeoJsonUtils
                .featureCollection(new GeoJsonFeatureCollection<Edge>()
                {
                    @Override
                    public Iterable<Edge> getGeoJsonObjects()
                    {
                        return Arrays.asList(edge, reverseEdge);
                    }

                    @Override
                    public JsonObject getGeoJsonProperties()
                    {
                        final JsonObject properties = new JsonObject();
                        properties.addProperty("aProperty", "aValue");
                        return properties;
                    }
                });
        Assert.assertEquals(FEATURE_COLLECTION_STRING, featureCollection.toString());
    }

    @Test
    public void testGeometryCollection()
    {

        final Edge edge = new CompleteEdge(1L, EDGE_POLYLINE,
                Collections.singletonMap("TheKey", "TheValue"), 0L, 1L, Collections.emptySet());
        final Edge reverseEdge = new CompleteEdge(-1L, EDGE_POLYLINE.reversed(),
                Collections.singletonMap("TheKey", "TheValue"), 1L, 0L, Collections.emptySet());

        final JsonObject geometryCollection = GeoJsonUtils
                .geometry(new GeojsonGeometryCollection<PolyLine>()
                {
                    @Override
                    public Iterable<PolyLine> getGeoJsonObjects()
                    {
                        return Arrays.asList(EDGE_POLYLINE, EDGE_POLYLINE.reversed());
                    }
                });
        final JsonObject geometryCollectionFromEdges = GeoJsonUtils
                .geometry(new GeojsonGeometryCollection<Edge>()
                {
                    @Override
                    public Iterable<Edge> getGeoJsonObjects()
                    {
                        return Arrays.asList(edge, reverseEdge);
                    }
                });

        Assert.assertEquals(GEOMETRY_COLLECTION_STRING, geometryCollection.toString());
        Assert.assertEquals(GEOMETRY_COLLECTION_STRING, geometryCollectionFromEdges.toString());
    }
}

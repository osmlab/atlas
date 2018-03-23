package org.openstreetmap.atlas.geography.geojson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.GeoJsonType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.streaming.readers.GeoJsonReader;
import org.openstreetmap.atlas.streaming.readers.json.serializers.PropertiesLocated;
import org.openstreetmap.atlas.streaming.resource.StringResource;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class GeoJsonBuilderTest
{
    @Test
    public void testConsistency()
    {
        final Polygon polygon = new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6);
        final GeoJsonObject object = new GeoJsonBuilder().create(polygon);
        final Map<String, String> properties = new HashMap<>();
        properties.put("property", "value");
        properties.put("property2", "value2");
        object.withNewProperties(properties);
        // The GeoJsonReader reads Feature collections only
        object.makeFeatureCollection();
        final GeoJsonReader reader = new GeoJsonReader(new StringResource(object.toString()));
        final PropertiesLocated item = reader.next();
        Assert.assertEquals(polygon, item.getItem());
    }

    @Test
    public void testFeatureCollection()
    {
        final Map<String, String> properties = new HashMap<>();
        properties.put("property", "value");
        properties.put("property2", "value2");
        final List<LocationIterableProperties> items = new ArrayList<>();
        items.add(new LocationIterableProperties(Location.TEST_1, properties));
        items.add(new LocationIterableProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new LocationIterableProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        final GeoJsonObject object = new GeoJsonBuilder().create(items);
        Assert.assertEquals(
                "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\""
                        + ":[-122.009566,37.33531]},\"properties\":{\"property2\":\"value2\",\"property\":\"value\"}},{\"type\":\"Feature\",\"geometry\""
                        + ":{\"type\":\"Polygon\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]]},\"properties\""
                        + ":{\"property2\":\"value2\",\"property\":\"value\"}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\""
                        + ":[[-122.031007,37.390535],[-122.028464,37.321628]]},\"properties\":{\"property2\":\"value2\",\"property\":\"value\"}}]}",
                object.toString());
    }

    @Test
    public void testGeometryCollectionSingularForm()
    {
        final Map<String, String> properties = new HashMap<>();
        final List<LocationIterableProperties> items = new ArrayList<>();
        items.add(new LocationIterableProperties(Location.TEST_1, properties));
        items.add(new LocationIterableProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new LocationIterableProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        final GeoJsonObject object = new GeoJsonBuilder().createGeometryCollection(items);
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\",\"coordinates\":[-122.009566,37.33531]},{\"type\":\"Polygon\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544]]]},{\"type\":\"LineString\",\"coordinates\":[[-122.031007,37.390535],[-122.028464,37.321628]]}]}}",
                object.toString());
    }

    @Test
    public void testGeometryCollectionMultipleForm()
    {
        final Map<String, String> properties = new HashMap<>();
        final List<LocationIterableProperties> items = new ArrayList<>();
        items.add(new LocationIterableProperties(Location.TEST_1, properties));
        items.add(new LocationIterableProperties(Location.TEST_1, properties));
        items.add(new LocationIterableProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new LocationIterableProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new LocationIterableProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        items.add(new LocationIterableProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        final GeoJsonObject object = new GeoJsonBuilder().createGeometryCollection(items);
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"MultiPoint\",\"coordinates\":[[-122.009566,37.33531],[-122.009566,37.33531]]},{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]],[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]]]},{\"type\":\"MultiLineString\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628]],[[-122.031007,37.390535],[-122.028464,37.321628]]]}]}}",
                object.toString());
    }

    @Test
    public void testLineString()
    {
        final GeoJsonObject object = new GeoJsonBuilder()
                .create(new PolyLine(Location.TEST_5, Location.TEST_2));
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-122.031007,37.390535],[-122.028464,37.321628]]}}",
                object.toString());

        final Map<String, String> properties = new HashMap<>();
        properties.put("property", "value");
        properties.put("property2", "value2");
        object.withNewProperties(properties);
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[-122.031007,37.390535],[-122.028464,37.321628]]},\"properties\":{\"property2\":\"value2\",\"property\":\"value\"}}",
                object.toString());
    }

    @Test
    public void testLocation()
    {
        final GeoJsonObject object = new GeoJsonBuilder().create(Location.TEST_5);
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.031007,37.390535]}}",
                object.toString());

        object.withNewProperty("property", "value");
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.031007,37.390535]},\"properties\":{\"property\":\"value\"}}",
                object.toString());
        object.withNewProperty("property2", "value2");
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-122.031007,37.390535]},\"properties\":{\"property\":\"value\",\"property2\":\"value2\"}}",
                object.toString());
    }

    @Test
    public void testMultiLineString()
    {
        final List<PolyLine> polyLines = new ArrayList<>();
        polyLines.add(new PolyLine(Location.TEST_5, Location.TEST_2));
        polyLines.add(new PolyLine(Location.COLOSSEUM, Location.EIFFEL_TOWER));

        final GeoJsonObject object = new GeoJsonBuilder().createMultiLineStrings(polyLines);

        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiLineString\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628]],[[12.49234,41.890224],[2.294495,48.858241]]]}}",
                object.toString());
    }

    @Test
    public void testMultiPoint()
    {
        final List<Location> locations = Arrays.asList(Location.EIFFEL_TOWER, Location.COLOSSEUM);
        final GeoJsonObject object = new GeoJsonBuilder().create(locations,
                GeoJsonType.MULTI_POINT);
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPoint\",\"coordinates\":[[2.294495,48.858241],[12.49234,41.890224]]}}",
                object.toString());

        object.withNewProperty("property", "value");
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPoint\",\"coordinates\":[[2.294495,48.858241],[12.49234,41.890224]]},\"properties\":{\"property\":\"value\"}}",
                object.toString());
        object.withNewProperty("property2", "value2");
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPoint\",\"coordinates\":[[2.294495,48.858241],[12.49234,41.890224]]},\"properties\":{\"property\":\"value\",\"property2\":\"value2\"}}",
                object.toString());
    }

    @Test
    public void testMultiPolygon()
    {
        final List<Polygon> polygons = new ArrayList<>();
        polygons.add(new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6));
        polygons.add(new Polygon(Location.COLOSSEUM, Location.EIFFEL_TOWER, Location.TEST_7));

        final GeoJsonObject object = new GeoJsonBuilder().createMultiPolygons(polygons);

        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]],[[[12.49234,41.890224],[2.294495,48.858241],[-122.0304871,37.3314171],[12.49234,41.890224]]]]}}",
                object.toString());
    }

    @Test
    public void testPolygon()
    {
        final GeoJsonObject object = new GeoJsonBuilder()
                .create(new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6));
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]]}}",
                object.toString());

        final Map<String, String> properties = new HashMap<>();
        properties.put("property", "value");
        properties.put("property2", "value2");
        object.withNewProperties(properties);
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]]},\"properties\":{\"property2\":\"value2\",\"property\":\"value\"}}",
                object.toString());
    }
}

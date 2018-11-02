package org.openstreetmap.atlas.geography.geojson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.GeoJsonType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.GeometryWithProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.streaming.readers.GeoJsonReader;
import org.openstreetmap.atlas.streaming.readers.json.serializers.PropertiesLocated;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author matthieun
 * @author mgostintsev
 * @author rmegraw
 */
@SuppressWarnings("deprecation")
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
    public void testCreateFeatureCollectionFromPropertiesLocated()
    {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("prop1", "foo");
        properties.put("prop2", new Float[] { 1.0F, 2.0F, 3.0F });
        properties.put("prop3", 0);

        final JsonObject propertiesObject = new JsonObject();
        final Gson gson = new Gson();
        properties.forEach((key, value) -> propertiesObject.add(key, gson.toJsonTree(value)));
        propertiesObject.add("properties", propertiesObject);

        final PropertiesLocated propertiesLocated1 = new PropertiesLocated(PolyLine.TEST_POLYLINE,
                propertiesObject);
        final PropertiesLocated propertiesLocated2 = new PropertiesLocated(PolyLine.TEST_POLYLINE,
                propertiesObject);

        final GeoJsonObject featureCollection = new GeoJsonBuilder()
                .createFeatureCollectionFromPropertiesLocated(
                        Iterables.from(propertiesLocated1, propertiesLocated2));

        Assert.assertEquals("FeatureCollection",
                featureCollection.jsonObject().get("type").getAsString());
        Assert.assertEquals(2,
                featureCollection.jsonObject().get("features").getAsJsonArray().size());
        for (int i = 0; i < 2; i++)
        {
            Assert.assertEquals("Feature", featureCollection.jsonObject().get("features")
                    .getAsJsonArray().get(i).getAsJsonObject().get("type").getAsString());
            Assert.assertEquals("LineString",
                    featureCollection.jsonObject().get("features").getAsJsonArray().get(i)
                            .getAsJsonObject().get("geometry").getAsJsonObject().get("type")
                            .getAsString());
            Assert.assertEquals(
                    PolyLine.TEST_POLYLINE.asGeoJson().jsonObject().get("features").getAsJsonArray()
                            .get(0).getAsJsonObject().get("geometry"),
                    featureCollection.jsonObject().get("features").getAsJsonArray().get(i)
                            .getAsJsonObject().get("geometry").getAsJsonObject());
            Assert.assertEquals(properties.get("prop1"),
                    featureCollection.jsonObject().get("features").getAsJsonArray().get(i)
                            .getAsJsonObject().get("properties").getAsJsonObject().get("prop1")
                            .getAsString());
            Assert.assertEquals(((Float[]) properties.get("prop2")).length,
                    featureCollection.jsonObject().get("features").getAsJsonArray().get(i)
                            .getAsJsonObject().get("properties").getAsJsonObject().get("prop2")
                            .getAsJsonArray().size());
            for (int j = i; j < 3; j++)
            {
                Assert.assertEquals(((Float[]) properties.get("prop2"))[j],
                        featureCollection.jsonObject().get("features").getAsJsonArray().get(i)
                                .getAsJsonObject().get("properties").getAsJsonObject().get("prop2")
                                .getAsJsonArray().get(j).getAsFloat(),
                        0D);
            }
            Assert.assertEquals(properties.get("prop3"),
                    featureCollection.jsonObject().get("features").getAsJsonArray().get(i)
                            .getAsJsonObject().get("properties").getAsJsonObject().get("prop3")
                            .getAsInt());
        }
    }

    @Test
    public void testCreateFromGeometries()
    {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("property", "value");
        properties.put("property2", "value2");
        final List<GeometryWithProperties> items = new ArrayList<>();
        items.add(new GeometryWithProperties(Location.TEST_1, properties));
        items.add(new GeometryWithProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new GeometryWithProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        final GeoJsonObject object = new GeoJsonBuilder().createFromGeometriesWithProperties(items);
        Assert.assertEquals(
                "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\""
                        + ":[-122.009566,37.33531]},\"properties\":{\"property2\":\"value2\",\"property\":\"value\"}},{\"type\":\"Feature\",\"geometry\""
                        + ":{\"type\":\"Polygon\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]]},\"properties\""
                        + ":{\"property2\":\"value2\",\"property\":\"value\"}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\""
                        + ":[[-122.031007,37.390535],[-122.028464,37.321628]]},\"properties\":{\"property2\":\"value2\",\"property\":\"value\"}}]}",
                object.toString());
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
    public void testGeometryCollectionFeature()
    {
        final Map<String, Object> properties = new HashMap<>();
        final List<GeometryWithProperties> items = new ArrayList<>();
        items.add(new GeometryWithProperties(Location.TEST_1, properties));
        items.add(new GeometryWithProperties(Location.TEST_1, properties));
        items.add(new GeometryWithProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new GeometryWithProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new GeometryWithProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        items.add(new GeometryWithProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        final GeoJsonObject object = new GeoJsonBuilder().createGeometryCollectionFeature(items);
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"MultiPoint\",\"coordinates\":[[-122.009566,37.33531],[-122.009566,37.33531]]},{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]],[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]]]},{\"type\":\"MultiLineString\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628]],[[-122.031007,37.390535],[-122.028464,37.321628]]]}]}}",
                object.toString());
    }

    @Test
    public void testGeometryCollectionFeatureMultipleForm()
    {
        final Map<String, Object> properties = new HashMap<>();
        final List<GeometryWithProperties> items = new ArrayList<>();
        items.add(new GeometryWithProperties(Location.TEST_1, properties));
        items.add(new GeometryWithProperties(Location.TEST_1, properties));
        items.add(new GeometryWithProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new GeometryWithProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new GeometryWithProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        items.add(new GeometryWithProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        final GeoJsonObject object = new GeoJsonBuilder().createGeometryCollectionFeature(items);
        Assert.assertEquals(
                "{\"type\":\"Feature\",\"geometry\":{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"MultiPoint\",\"coordinates\":[[-122.009566,37.33531],[-122.009566,37.33531]]},{\"type\":\"MultiPolygon\",\"coordinates\":[[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]],[[[-122.031007,37.390535],[-122.028464,37.321628],[-122.033948,37.32544],[-122.031007,37.390535]]]]},{\"type\":\"MultiLineString\",\"coordinates\":[[[-122.031007,37.390535],[-122.028464,37.321628]],[[-122.031007,37.390535],[-122.028464,37.321628]]]}]}}",
                object.toString());
    }

    @Test
    public void testGeometryCollectionFeatureSingularForm()
    {
        final Map<String, Object> properties = new HashMap<>();
        final List<GeometryWithProperties> items = new ArrayList<>();
        items.add(new GeometryWithProperties(Location.TEST_1, properties));
        items.add(new GeometryWithProperties(
                new Polygon(Location.TEST_5, Location.TEST_2, Location.TEST_6), properties));
        items.add(new GeometryWithProperties(new PolyLine(Location.TEST_5, Location.TEST_2),
                properties));
        final GeoJsonObject object = new GeoJsonBuilder().createGeometryCollectionFeature(items);
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

    @Test
    public void testPropertiesObjects()
    {
        final List<Integer> list = Arrays.asList(new Integer[] { 1, 2, 3 });
        final Map<String, Object> properties = new HashMap<>();
        properties.put("prop1", 1);
        properties.put("prop2", list);
        properties.put("prop3", "mystring");
        final GeometryWithProperties geometryWithProperties = new GeometryWithProperties(
                PolyLine.TEST_POLYLINE, properties);
        final JsonObject jsonObject = new GeoJsonBuilder().create(geometryWithProperties);
        Assert.assertEquals(1,
                jsonObject.get("properties").getAsJsonObject().get("prop1").getAsInt());
        final List<Integer> resultList = new ArrayList<>();
        for (final JsonElement element : jsonObject.get("properties").getAsJsonObject().get("prop2")
                .getAsJsonArray())
        {
            resultList.add(element.getAsInt());
        }
        Assert.assertEquals(list, resultList);
        Assert.assertEquals("mystring",
                jsonObject.get("properties").getAsJsonObject().get("prop3").getAsString());
    }

    @Test
    public void testToGeometryWithProperties()
    {
        final Map<String, String> stringProperties = new HashMap<>();
        stringProperties.put("prop1", "val1");
        stringProperties.put("prop2", "val2");
        final LocationIterableProperties locationIterableProperties = new LocationIterableProperties(
                PolyLine.TEST_POLYLINE, stringProperties);
        final GeometryWithProperties geometryWithProperties = GeoJsonBuilder
                .toGeometryWithProperties(locationIterableProperties);
        Assert.assertEquals(PolyLine.TEST_POLYLINE, geometryWithProperties.getGeometry());
        for (final Entry<String, String> stringPropertiesEntry : stringProperties.entrySet())
        {
            Assert.assertEquals(stringPropertiesEntry.getValue(),
                    geometryWithProperties.getProperties().get(stringPropertiesEntry.getKey()));
        }
    }
}

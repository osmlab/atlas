package org.openstreetmap.atlas.geography.geojson.parser.impl.jackson;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.geojson.parser.domain.base.GeoJsonItem;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox2D;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox3D;
import org.openstreetmap.atlas.geography.geojson.parser.domain.feature.Feature;
import org.openstreetmap.atlas.geography.geojson.parser.domain.feature.FeatureCollection;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.GeometryCollection;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.LineString;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiLineString;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiPoint;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.MultiPolygon;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Point;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.Polygon;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change.Descriptor;
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change.FeatureChangeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some of the examples are taken and modified from wikipedia and geojson spec.
 *
 * @author Yazad Khambata
 */
public class GeoJsonParserJacksonImplTest extends AbstractGeoJsonParserJacksonImplTestBase
{
    private static final Logger log = LoggerFactory.getLogger(GeoJsonParserJacksonImplTest.class);

    @Test
    public void feature1()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof Feature);
        Assert.assertTrue(((Feature) geoJsonItem).getGeometry() instanceof LineString);
    }

    @Test
    public void feature2()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof Feature);
        Assert.assertTrue(((Feature) geoJsonItem).getGeometry() instanceof Polygon);

    }

    @Test
    public void featureChangePropertiesExample1()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        final FeatureChangeProperties featureChangeProperties = geoJsonItem.getProperties()
                .asType(FeatureChangeProperties.class);

        Assert.assertNull(featureChangeProperties.getRelations());
    }

    @Test
    public void featureChangePropertiesExample2()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        final FeatureChangeProperties featureChangeProperties = geoJsonItem.getProperties()
                .asType(FeatureChangeProperties.class);

        final Descriptor descriptor = featureChangeProperties.getDescription().getDescriptors()[0];
        Assert.assertEquals(-1374305525422343588L, (long) descriptor.getId());
        Assert.assertEquals("NODE", descriptor.getItemType());
        Assert.assertEquals("via", descriptor.getRole());
        Assert.assertEquals(9087654321L, (long) descriptor.getBeforeElement());
        Assert.assertEquals(1234567890L, (long) descriptor.getAfterElement());
    }

    @Test
    public void featureCollection1()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof FeatureCollection);
        Assert.assertEquals(3, ((FeatureCollection) geoJsonItem).getFeatures().size());
    }

    @Test
    public void featureWithExtendedProperties()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof Feature);
        Assert.assertTrue(((Feature) geoJsonItem).getGeometry() instanceof LineString);

        final FeatureChangeProperties featureChangeProperties = geoJsonItem.getProperties()
                .asType(FeatureChangeProperties.class);

        Assert.assertEquals(9, featureChangeProperties.getDescription().getDescriptors().length);
        Assert.assertFalse(featureChangeProperties.getWKT().isEmpty());

        log.info("featureChangeProperties:: {}.", featureChangeProperties);
    }

    @Test
    public void foreignFieldsNested()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof Point);
    }

    @Test
    public void foreignFieldsSimple()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof Point);
        Assert.assertFalse(geoJsonItem.getForeignFields().asMap().isEmpty());
    }

    @Test
    public void geometryCollectionBasic()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof GeometryCollection);
        Assert.assertFalse(((GeometryCollection) geoJsonItem).getGeometries().isEmpty());
    }

    @Test
    public void geometryCollectionChildConversion()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof GeometryCollection);
        Assert.assertFalse(((GeometryCollection) geoJsonItem).getGeometries().isEmpty());
    }

    @Test
    public void geometryCollectionRecursiveNested()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        final int innermostLevelSize = (((GeometryCollection) ((GeometryCollection) ((GeometryCollection) ((GeometryCollection) geoJsonItem)
                .getGeometries().get(3)).getGeometries().get(3)).getGeometries().get(3)))
                        .getGeometries().size();

        Assert.assertEquals(3, innermostLevelSize);
    }

    @Test
    public void lineString()
    {
        Assert.assertTrue(toGeoJsonItem() instanceof LineString);
    }

    @Test
    public void lineStringConversion()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof LineString);
        final LineString lineString = (LineString) geoJsonItem;
        final PolyLine polyLine = lineString.toAtlasGeometry();
        Assert.assertEquals(3, lineString.getCoordinates().getValue().size());
        Assert.assertEquals(lineString.getCoordinates().getValue().size(), polyLine.size());
        final Location first = polyLine.first();
        Assert.assertEquals((Double) first.getLongitude().asDegrees(),
                lineString.getCoordinates().getValue().get(0).getCoordinate1());
        Assert.assertEquals((Double) first.getLatitude().asDegrees(),
                lineString.getCoordinates().getValue().get(0).getCoordinate2());
    }

    @Test
    public void multiLineString()
    {
        Assert.assertTrue(toGeoJsonItem() instanceof MultiLineString);
    }

    @Test
    public void multiLineStringConversion()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof MultiLineString);
        final MultiLineString multiLineString = (MultiLineString) geoJsonItem;
        final List<PolyLine> polyLines = multiLineString.toAtlasGeometry();
        Assert.assertEquals(2, multiLineString.getCoordinates().getValue().size());
        Assert.assertEquals(multiLineString.getCoordinates().getValue().size(), polyLines.size());
        final PolyLine firstPolyLine = polyLines.get(0);
        Assert.assertEquals(5, multiLineString.getCoordinates().getValue().get(0).size());
        Assert.assertEquals(multiLineString.getCoordinates().getValue().get(0).size(),
                firstPolyLine.size());
    }

    @Test
    public void multiPoint()
    {
        Assert.assertTrue(toGeoJsonItem() instanceof MultiPoint);
    }

    @Test
    public void multiPointConversion()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof MultiPoint);
        final MultiPoint multiPoint = (MultiPoint) geoJsonItem;
        final List<Location> locations = multiPoint.toAtlasGeometry();
        Assert.assertEquals(3, multiPoint.getCoordinates().getValue().size());
        Assert.assertEquals(multiPoint.getCoordinates().getValue().size(), locations.size());
        final Location first = locations.get(0);
        Assert.assertEquals((Double) first.getLongitude().asDegrees(),
                multiPoint.getCoordinates().getValue().get(0).getCoordinate1());
        Assert.assertEquals((Double) first.getLatitude().asDegrees(),
                multiPoint.getCoordinates().getValue().get(0).getCoordinate2());
    }

    @Test
    public void multiPolygon()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoJsonItem;
        final org.openstreetmap.atlas.geography.MultiPolygon atlasPolygons = multiPolygon
                .toAtlasGeometry();
        Assert.assertEquals(1, atlasPolygons.outers().size());
        Assert.assertTrue(atlasPolygons.inners().isEmpty());
    }

    @Test
    public void multiPolygonConversion()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoJsonItem;
        final org.openstreetmap.atlas.geography.MultiPolygon atlasPolygons = multiPolygon
                .toAtlasGeometry();
        Assert.assertEquals(2, multiPolygon.getCoordinates().getValue().size());
        Assert.assertEquals(multiPolygon.getCoordinates().getValue().size(),
                atlasPolygons.outers().size());
        Assert.assertEquals(4, atlasPolygons.inners().size());
    }

    @Test
    public void multiPolygonDonut()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof MultiPolygon);
        final MultiPolygon multiPolygon = (MultiPolygon) geoJsonItem;
        final org.openstreetmap.atlas.geography.MultiPolygon atlasMultiPolygon = multiPolygon
                .toAtlasGeometry();
        Assert.assertEquals(1, multiPolygon.toAtlasGeometry().inners().size());
    }

    @Test
    public void point()
    {
        Assert.assertTrue(toGeoJsonItem() instanceof Point);
    }

    @Test
    public void pointConversion()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof Point);

        final Point point = (Point) geoJsonItem;
        final Location location = point.toAtlasGeometry();
        Assert.assertNotNull(location);
        log.info("Location: {}.", location);
        Assert.assertEquals((Double) location.getLongitude().asDegrees(),
                point.getCoordinates().getValue().getCoordinate1());
        Assert.assertEquals((Double) location.getLatitude().asDegrees(),
                point.getCoordinates().getValue().getCoordinate2());
    }

    @Test
    public void pointWithBbox2D()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof Point);
        Assert.assertTrue(geoJsonItem.getBbox() instanceof Bbox2D);
    }

    @Test
    public void pointWithBbox3D()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof Point);
        Assert.assertTrue(geoJsonItem.getBbox() instanceof Bbox3D);
    }

    @Test
    public void polygon()
    {
        Assert.assertTrue(toGeoJsonItem() instanceof Polygon);
    }

    @Test
    public void polygonConversion()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof Polygon);
        final Polygon polygon = (Polygon) geoJsonItem;
        final org.openstreetmap.atlas.geography.Polygon atlasPolygon = polygon.toAtlasGeometry();
        log.info("Atlas Polygon: {}.", atlasPolygon);
        Assert.assertEquals(5, polygon.getCoordinates().getValue().get(0).size());
        Assert.assertEquals(polygon.getCoordinates().getValue().get(0).size(), atlasPolygon.size());
    }

    @Test
    public void propertiesNested()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertEquals(3,
                ((Map<String, Object>) geoJsonItem.getProperties().get("prop0")).size());
    }

    @Test
    public void propertiesSimple()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertEquals(2, geoJsonItem.getProperties().asMap().size());
    }
}

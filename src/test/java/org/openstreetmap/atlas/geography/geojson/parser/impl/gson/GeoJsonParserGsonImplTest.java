package org.openstreetmap.atlas.geography.geojson.parser.impl.gson;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
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
import org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change.FeatureChangeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some of the examples are taken and modified from wikipedia and geojson spec.
 *
 * @author Yazad Khambata
 */
public class GeoJsonParserGsonImplTest extends AbstractGeoJsonParserGsonImplTest
{
    private static final Logger log = LoggerFactory.getLogger(GeoJsonParserGsonImplTest.class);

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
    public void featureChangePropertiesExample()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        final FeatureChangeProperties featureChangeProperties = geoJsonItem.getProperties()
                .asType(FeatureChangeProperties.class);

        Assert.assertNull(featureChangeProperties.getRelations());
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
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof LineString);
    }

    @Test
    public void multiLineString()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof MultiLineString);
    }

    @Test
    public void multiPoint()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof MultiPoint);
    }

    @Test
    public void multiPolygon()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof MultiPolygon);
    }

    @Test
    public void point()
    {
        final GeoJsonItem geoJsonItem = toGeoJsonItem();

        Assert.assertTrue(geoJsonItem instanceof Point);
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
        final GeoJsonItem geoJsonItem = toGeoJsonItem();
        Assert.assertTrue(geoJsonItem instanceof Polygon);
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

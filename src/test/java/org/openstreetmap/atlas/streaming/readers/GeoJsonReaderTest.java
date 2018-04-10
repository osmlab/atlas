package org.openstreetmap.atlas.streaming.readers;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.streaming.readers.json.serializers.PropertiesLocated;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class GeoJsonReaderTest
{
    private static final Logger logger = LoggerFactory.getLogger(GeoJsonReaderTest.class);

    @Test
    public void testReadingMulti()
    {
        final GeoJsonReader reader = new GeoJsonReader(new InputStreamResource(
                GeoJsonReaderTest.class.getResourceAsStream("geojson-sample.json")));
        reader.forEachRemaining(located -> logger.info(located.toString()));
    }

    @Test
    public void testReadingPolygon()
    {
        final GeoJsonReader reader = new GeoJsonReader(new InputStreamResource(
                GeoJsonReaderTest.class.getResourceAsStream("geojson-polygon.json")));

        Assert.assertTrue(reader.hasNext());
        final PropertiesLocated located = reader.next();
        final Polygon polygon = (Polygon) located.getItem();
        Assert.assertEquals(4, polygon.segments().size());
        Assert.assertTrue(polygon.finalHeading().isPresent());
        Assert.assertTrue(polygon.isApproximatelyNSided(4, Angle.NONE));
    }

    @Test
    public void testReadingMultiPolygon()
    {
        final GeoJsonReader reader = new GeoJsonReader(new InputStreamResource(
                GeoJsonReaderTest.class.getResourceAsStream("geojson-multipolygon.json")));

        Assert.assertTrue(reader.hasNext());
        final PropertiesLocated located = reader.next();
        final MultiPolygon multiPolygon = (MultiPolygon) located.getItem();
        Assert.assertEquals(2, multiPolygon.outers().size());
        Assert.assertEquals(1, multiPolygon.inners().size());

        // test non-duplicated first point
        for (final Polygon polygon : multiPolygon)
        {
            Assert.assertTrue(polygon.finalHeading().isPresent());
        }
        logger.info(located.toString());
    }

    @Test
    public void testReadingPoint()
    {
        final GeoJsonReader reader = new GeoJsonReader(new InputStreamResource(
                GeoJsonReaderTest.class.getResourceAsStream("geojson-point.json")));
        reader.forEachRemaining(located -> logger.info(located.toString()));
    }
}

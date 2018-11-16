package org.openstreetmap.atlas.geography;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.maps.MultiMapTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author matthieun
 * @author hallahan
 */
public class MultiPolygonTest
{
    private static final Logger logger = LoggerFactory.getLogger(MultiPolygonTest.class);

    @Test
    public void testCoversPolygon()
    {
        final MultiPolygon multiPolygon = MultiPolygon
                .wkt("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),"
                        + "((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),"
                        + "(30 20, 20 15, 20 25, 30 20)))");
        logger.info("multiPolygon: {}", multiPolygon.toWkt());
        final Polygon coveringPolygon = new Polygon(Location.forString("48.861903, 2.344141"),
                Location.forString("6.215559, 1.431353"),
                Location.forString("-1.302400, 36.818213"),
                Location.forString("22.648164, 50.364465"));
        logger.info("coveringPolygon: {}", coveringPolygon.toWkt());
        Assert.assertTrue(multiPolygon.overlaps(coveringPolygon));
        Assert.assertFalse(multiPolygon.intersects(coveringPolygon));

        final Polygon insideInnerPolygon = new Polygon(Location.forString("20.146558, 23.310950"),
                Location.forString("19.623812, 24.507328"),
                Location.forString("19.247746, 23.339148"));
        logger.info("insideInnerPolygon: {}", insideInnerPolygon.toWkt());
        Assert.assertFalse(multiPolygon.overlaps(insideInnerPolygon));
        Assert.assertFalse(multiPolygon.intersects(insideInnerPolygon));

        final Polygon intersectingInnerPolygon = new Polygon(
                Location.forString("20.146558, 23.310950"),
                Location.forString("19.623812, 24.507328"),
                Location.forString("27.156014, 30.298381"));
        logger.info("intersectingInnerPolygon: {}", intersectingInnerPolygon.toWkt());
        Assert.assertTrue(multiPolygon.overlaps(intersectingInnerPolygon));
        Assert.assertTrue(multiPolygon.intersects(intersectingInnerPolygon));

        final Polygon intersectingOuterPolygon = new Polygon(
                Location.forString("48.861903, 2.344141"),
                Location.forString("22.648164, 50.364465"),
                Location.forString("27.156014, 30.298381"));
        logger.info("intersectingOuterPolygon: {}", intersectingOuterPolygon.toWkt());
        Assert.assertTrue(multiPolygon.overlaps(intersectingInnerPolygon));
        Assert.assertTrue(multiPolygon.intersects(intersectingInnerPolygon));
    }

    @Test
    public void testCoversPolyLine()
    {
        final MultiPolygon multiPolygon = MultiPolygon
                .wkt("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),"
                        + "((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),"
                        + "(30 20, 20 15, 20 25, 30 20)))");
        final PolyLine nonCoveredLine = new PolyLine(Location.forString("19.507134, 23.999584"),
                Location.forString("32.062144, 32.049430"));
        Assert.assertFalse(multiPolygon.fullyGeometricallyEncloses(nonCoveredLine));
        final PolyLine coveredLine = new PolyLine(Location.forString("32.095270, 19.955880"),
                Location.forString("22.175213, 36.610574"));
        Assert.assertTrue(multiPolygon.fullyGeometricallyEncloses(coveredLine));
        final PolyLine outside = PolyLine.TEST_POLYLINE;
        Assert.assertFalse(multiPolygon.fullyGeometricallyEncloses(outside));
        final PolyLine jumpyLine = new PolyLine(Location.forString("32.095270, 19.955880"),
                Location.forString("39.927387, 32.842205"));
        Assert.assertFalse(multiPolygon.fullyGeometricallyEncloses(jumpyLine));
    }

    @Test
    public void testEmptyMultiPolygon()
    {
        final MultiPolygon empty = new MultiPolygon(new MultiMap<>());
        Assert.assertNull(empty.bounds());
    }

    @Test
    public void testFullyGeometricallyEncloses()
    {
        final MultiPolygon multiPolygon1 = MultiPolygon
                .wkt("MULTIPOLYGON (" + "((-10 10, 10 10, 10 -10, -10 -10, -10 10),"
                        + "(-5 5, 5 5, 5 -5, -5 -5, -5 5)))");
        final MultiPolygon multiPolygon2 = MultiPolygon.wkt("MULTIPOLYGON ("
                + "((-8 8, 8 8, 8 -8, -8 -8, -8 8)," + "(-6 6, 6 6, 6 -6, -6 -6, -6 6)))");
        final MultiPolygon multiPolygon3 = MultiPolygon.wkt("MULTIPOLYGON ("
                + "((-8 8, 8 8, 8 -8, -8 -8, -8 8)," + "(-4 4, 4 4, 4 -4, -4 -4, -4 4)))");
        final MultiPolygon multiPolygon4 = MultiPolygon
                .wkt("MULTIPOLYGON (" + "((-8 8, 8 8, 8 -8, -8 -8, -8 8)))");
        final MultiPolygon multiPolygon5 = MultiPolygon.wkt("MULTIPOLYGON ("
                + "((-4 4, 4 4, 4 -4, -4 -4, -4 4)," + "(-3 3, 3 3, 3 -3, -3 -3, -3 3)))");
        final MultiPolygon multiPolygon6 = MultiPolygon
                .wkt("MULTIPOLYGON (" + "((8 9, 9 9, 9 8, 8 8, 8 9),"
                        + "(8.25 8.75, 8.75 8.75, 8.75 8.25, 8.25 8.25, 8.25 8.75)))");
        final MultiPolygon multiPolygon7 = MultiPolygon
                .wkt("MULTIPOLYGON (" + "((89 90, 90 90, 90 89, 89 89, 89 90),"
                        + "(89.25 89.75, 89.75 89.75, 89.75 89.25, 89.25 89.25, 89.25 89.75)))");
        // Test Multipolygon enclose
        Assert.assertTrue(multiPolygon1.fullyGeometricallyEncloses(multiPolygon2));
        Assert.assertFalse(multiPolygon2.fullyGeometricallyEncloses(multiPolygon1));
        Assert.assertFalse(multiPolygon1.fullyGeometricallyEncloses(multiPolygon3));
        Assert.assertFalse(multiPolygon1.fullyGeometricallyEncloses(multiPolygon4));
        Assert.assertFalse(multiPolygon1.fullyGeometricallyEncloses(multiPolygon5));
        Assert.assertFalse(multiPolygon5.fullyGeometricallyEncloses(multiPolygon1));
        Assert.assertFalse(
                multiPolygon1.fullyGeometricallyEncloses(multiPolygon2.merge(multiPolygon5)));
        Assert.assertTrue(multiPolygon1.fullyGeometricallyEncloses(multiPolygon6));
        Assert.assertFalse(multiPolygon1.fullyGeometricallyEncloses(multiPolygon7));

        final PolyLine polyLine1 = PolyLine.wkt("LINESTRING (6 6, -6 -6)");
        final PolyLine polyLine2 = PolyLine.wkt("LINESTRING (6 6, -6 6, -6 -6, 6 -6)");
        final Polygon polygon1 = Polygon.wkt("POLYGON ((6 6, -6 6, -6 -6, 6 -6, 6 6))");

        // Test Polyline Enclose
        Assert.assertFalse(multiPolygon1.fullyGeometricallyEncloses(polyLine1));
        Assert.assertTrue(multiPolygon1.fullyGeometricallyEncloses(polyLine2));
        Assert.assertFalse(multiPolygon1.fullyGeometricallyEncloses(polygon1));
    }

    @Test
    public void testOverlap()
    {
        final MultiPolygon multiPolygon1 = MultiPolygon.wkt("MULTIPOLYGON ("
                + "((0 50, 50 50, 50 0, 0 0, 0 50)," + "(5 45, 45 45, 45 5, 5 5, 5 45)))");
        final MultiPolygon multiPolygon2 = MultiPolygon
                .wkt("MULTIPOLYGON (" + "((10 35, 35 35, 35 10, 10 10, 10 35),"
                        + "(15 30, 30 30, 30 15, 15 15, 15 30)))");
        final MultiPolygon multiPolygon3 = MultiPolygon
                .wkt("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),"
                        + "((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),"
                        + "(30 20, 20 15, 20 25, 30 20)))");

        Assert.assertFalse(multiPolygon1.overlaps(multiPolygon2));
        Assert.assertFalse(multiPolygon2.overlaps(multiPolygon1));
        Assert.assertTrue(multiPolygon2.overlaps(multiPolygon3));
        Assert.assertTrue(multiPolygon3.overlaps(multiPolygon2));
    }

    @Test
    public void testSerialization() throws ClassNotFoundException
    {
        final MultiPolygon map = new MultiPolygon(MultiMapTest.getMultiMap());
        final WritableResource out = new ByteArrayResource();
        try (ObjectOutputStream outStream = new ObjectOutputStream(out.write()))
        {
            outStream.writeObject(map);
        }
        catch (final IOException e)
        {
            throw new CoreException("Unable to write to {}", out, e);
        }

        try (ObjectInputStream inStream = new ObjectInputStream(out.read()))
        {
            final MultiPolygon result = (MultiPolygon) inStream.readObject();
            Assert.assertEquals(map, result);
        }
        catch (final IOException e)
        {
            throw new CoreException("Unable to read from {}", out, e);
        }
    }

    // Adapted from PolygonTest.testSurface()
    @Test
    public void testSurface()
    {
        final Rectangle rectangle = Rectangle.TEST_RECTANGLE;
        Assert.assertEquals(rectangle.surface(), MultiPolygon.forPolygon(rectangle).surface());

        final MultiPolygon tilted = MultiPolygon.forPolygon(new Polygon(rectangle.lowerLeft(),
                new Location(rectangle.upperLeft().getLatitude(),
                        Longitude.degrees(rectangle.upperLeft().getLongitude().asDegrees() + 0.1)),
                new Location(rectangle.upperRight().getLatitude(),
                        Longitude.degrees(rectangle.upperRight().getLongitude().asDegrees() + 0.1)),
                rectangle.lowerRight()));
        Assert.assertEquals(rectangle.surface(), tilted.surface());
    }

    @Test
    public void testAsGeoJsonGeometry()
    {
        final MultiPolygon multiPolygon = MultiPolygon
                .wkt("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)),"
                        + "((20 35, 10 30, 10 10, 30 5, 45 20, 20 35),"
                        + "(30 20, 20 15, 20 25, 30 20)))");
        final String geoJson = "{\"type\":\"MultiPolygon\",\"coordinates\":[[[[40.0,40.0],[20.0,45.0],[45.0,30.0],[40.0,40.0]]],[[[20.0,35.0],[10.0,30.0],[10.0,10.0],[30.0,5.0],[45.0,20.0],[20.0,35.0]],[[30.0,20.0],[20.0,15.0],[20.0,25.0],[30.0,20.0]]]]}";
        final JsonObject geometry = multiPolygon.asGeoJsonGeometry();
        Assert.assertEquals(geoJson, geometry.toString());
    }
}

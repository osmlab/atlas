package org.openstreetmap.atlas.geography;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class PolygonTest
{
    private static final Logger logger = LoggerFactory.getLogger(PolygonTest.class);

    private Polygon quadrant;
    private Rectangle rectangle;

    @Rule
    public final PolygonTestRule setup = new PolygonTestRule();

    @Before
    public void init()
    {
        this.quadrant = new Polygon(Location.TEST_3, Location.TEST_4, Location.TEST_1,
                Location.TEST_2);
        this.rectangle = Rectangle
                .forLocations(Iterables.iterable(Location.TEST_5, Location.TEST_3));
    }

    @Test
    public void testAngle()
    {
        final PolyLine polyLine = new PolyLine(this.quadrant);
        Assert.assertEquals(Angle.degrees(148.2126213), polyLine.maximumAngle());
    }

    @Test
    public void testAngleLocation()
    {
        final PolyLine polyLine = new PolyLine(this.quadrant);
        Assert.assertEquals(Location.forString("37.33531,-122.009566"),
                polyLine.maximumAngleLocation().get());
    }

    @Test
    public void testCenter()
    {
        // test for rectangle
        final Polygon rectangle = new Polygon(Rectangle.TEST_RECTANGLE);
        final Location center = rectangle.center();
        Assert.assertEquals(center, Location.forString("37.3292805,-122.030478"));
        // test for triangle
        final Polygon triangle = new Polygon(Arrays.asList(Location.forString("1,1"),
                Location.forString("2,2"), Location.forString("1,2")));
        final Location triangleCenter = triangle.center();
        Assert.assertEquals(triangleCenter, Location.forString("1.3333333,1.6666667"));
    }

    @Test
    public void testClockwise()
    {
        Assert.assertTrue(this.quadrant.isClockwise());
        Assert.assertFalse(this.quadrant.reversed().isClockwise());
    }

    @Test
    public void testConcave()
    {
        final Polygon concave = new Polygon(Location.forString("1, -1"), Location.forString("1,1"),
                Location.forString("-1,1"), Location.forString("0,0"), Location.forString("-1,-1"));
        final Location outside1 = Location.forString("-1, 0");
        final Location outside2 = Location.forString("0, 2");
        final Location outside3 = Location.forString("-2, 0");
        final Location inside1 = Location.forString("-0.5, 0.8");
        final Location onBoundary1 = Location.forString("-0.5, 0.5");
        final Location onBoundary2 = Location.forString("0, 1");

        Assert.assertTrue(concave.fullyGeometricallyEncloses(inside1));
        Assert.assertTrue(concave.fullyGeometricallyEncloses(onBoundary1));
        Assert.assertFalse(concave.fullyGeometricallyEncloses(onBoundary2));
        Assert.assertFalse(concave.fullyGeometricallyEncloses(outside1));
        Assert.assertFalse(concave.fullyGeometricallyEncloses(outside2));
        Assert.assertTrue(concave.intersects(new Segment(outside1, inside1)));
        Assert.assertTrue(concave.intersects(new Segment(outside1, outside2)));
        Assert.assertFalse(concave.intersects(new Segment(outside1, outside3)));
    }

    @Test
    public void testContains()
    {
        final Rectangle rectangle = Rectangle.TEST_RECTANGLE;
        final Polygon polygon = new Polygon(rectangle);
        Assert.assertTrue("Checking that the Location exists in the Polygon",
                polygon.contains(Rectangle.TEST_RECTANGLE.first()));
        Assert.assertTrue("Checking that the Segent that closes the Polygon exists",
                polygon.contains(new Segment(Rectangle.TEST_RECTANGLE.last(),
                        Rectangle.TEST_RECTANGLE.first())));
    }

    @Test
    public void testCovers()
    {
        final Rectangle rwac = Rectangle
                .forLocations(Iterables.iterable(Location.TEST_3, Location.TEST_1));

        logger.info("Polygon: " + this.quadrant);

        final boolean containsTest6 = this.quadrant.fullyGeometricallyEncloses(Location.TEST_6);
        final boolean containsTest5 = this.quadrant.fullyGeometricallyEncloses(Location.TEST_5);
        final boolean containsTestRectangle = this.quadrant
                .fullyGeometricallyEncloses(Rectangle.TEST_RECTANGLE_2);
        final boolean containsRWAC = this.quadrant.fullyGeometricallyEncloses(rwac);

        logger.info("Test 6: {} -> {}", Location.TEST_6, containsTest6);
        logger.info("Test 5: {} -> {}", Location.TEST_5, containsTest5);
        logger.info("Test Rectangle: {} -> {}", Rectangle.TEST_RECTANGLE_2, containsTestRectangle);
        logger.info("RWAC 2: {} -> {}", rwac, containsRWAC);

        Assert.assertTrue(!containsTest5);
        Assert.assertTrue(containsTest6);
        Assert.assertTrue(containsTestRectangle);
        Assert.assertTrue(!containsRWAC);
    }

    @Test
    public void testCoversMultiPolygon()
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
        Assert.assertTrue(coveringPolygon.overlaps(multiPolygon));

        final Polygon insideInnerPolygon = new Polygon(Location.forString("20.146558, 23.310950"),
                Location.forString("19.623812, 24.507328"),
                Location.forString("19.247746, 23.339148"));
        logger.info("insideInnerPolygon: {}", insideInnerPolygon.toWkt());
        Assert.assertFalse(insideInnerPolygon.overlaps(multiPolygon));

        final Polygon intersectingInnerPolygon = new Polygon(
                Location.forString("20.146558, 23.310950"),
                Location.forString("19.623812, 24.507328"),
                Location.forString("27.156014, 30.298381"));
        logger.info("intersectingInnerPolygon: {}", intersectingInnerPolygon.toWkt());
        Assert.assertTrue(intersectingInnerPolygon.overlaps(multiPolygon));

        final Polygon intersectingOuterPolygon = new Polygon(
                Location.forString("48.861903, 2.344141"),
                Location.forString("22.648164, 50.364465"),
                Location.forString("27.156014, 30.298381"));
        logger.info("intersectingOuterPolygon: {}", intersectingOuterPolygon.toWkt());
        Assert.assertTrue(intersectingOuterPolygon.overlaps(multiPolygon));
    }

    @Test
    public void testFindingAnglesGreaterThanTarget()
    {
        final PolyLine polyLine = new PolyLine(this.quadrant);

        // Find all angles greater than or equal to 140 degrees.
        final List<Tuple<Angle, Location>> result = polyLine
                .anglesGreaterThanOrEqualTo(Angle.degrees(140));

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Tuple.createTuple(Angle.degrees(148.2126213),
                Location.forString("37.33531,-122.009566")), result.get(0));
    }

    @Test
    public void testFindingAnglesLessThanTarget()
    {
        final PolyLine polyLine = new PolyLine(this.quadrant);

        // Find all angles less than or equal to 30 degrees.
        final List<Tuple<Angle, Location>> result = polyLine
                .anglesLessThanOrEqualTo(Angle.degrees(30));

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Tuple.createTuple(Angle.degrees(28.3372516),
                Location.forString("37.332451,-122.028932")), result.get(0));
    }

    @Test
    public void testIntersects()
    {
        final Set<Location> intersection = this.quadrant.intersections(new Segment(Location.TEST_5,
                new Location(Location.TEST_3.getLatitude(), Location.TEST_5.getLongitude())));
        Assert.assertEquals(2, intersection.size());

        final Set<Location> intersections = this.quadrant.intersections(this.rectangle);
        Assert.assertEquals(3, intersections.size());

        Assert.assertTrue(this.quadrant.intersects(this.rectangle));
    }

    @Test
    public void testNoAnglesMatchingOurCriteria()
    {
        final PolyLine polyLine = new PolyLine(this.quadrant);

        // Find all angles greater than or equal to 140 degrees.
        final List<Tuple<Angle, Location>> result = polyLine
                .anglesGreaterThanOrEqualTo(Angle.degrees(179));

        Assert.assertTrue(result.size() == 0);
    }

    @Test
    public void testOffset()
    {
        Assert.assertEquals(Location.TEST_3, this.quadrant.offsetFromStart(Ratio.MINIMUM));
        Assert.assertEquals(Location.TEST_3, this.quadrant.offsetFromStart(Ratio.MAXIMUM));
        Assert.assertEquals(Location.forString("37.3352356,-122.0096688"), this.quadrant.middle());
    }

    @Test
    public void testRandom()
    {
        final Rectangle bounds = Rectangle.TEST_RECTANGLE;
        final Polygon randomPolygon = Polygon.random(50, bounds);
        for (final Location point : randomPolygon)
        {
            Assert.assertTrue(bounds.fullyGeometricallyEncloses(point));
        }
        final PolyLine randomPolyLine = PolyLine.random(50, bounds);
        for (final Location point : randomPolyLine)
        {
            Assert.assertTrue(bounds.fullyGeometricallyEncloses(point));
        }
    }

    @Test
    public void testSurface()
    {
        final Rectangle rectangle = Rectangle.TEST_RECTANGLE;
        Assert.assertEquals(rectangle.surface(), new Polygon(rectangle).surface());

        final Polygon tilted = new Polygon(rectangle.lowerLeft(),
                new Location(rectangle.upperLeft().getLatitude(),
                        Longitude.degrees(rectangle.upperLeft().getLongitude().asDegrees() + 0.1)),
                new Location(rectangle.upperRight().getLatitude(),
                        Longitude.degrees(rectangle.upperRight().getLongitude().asDegrees() + 0.1)),
                rectangle.lowerRight());
        Assert.assertEquals(rectangle.surface(), tilted.surface());
    }

    @Test
    public void testSurfaceOnSphere()
    {
        // OSM way 58674551
        final Polygon tennisCourt = new Polygon(Location.forString("47.6778433, -122.2012807"),
                Location.forString("47.6779773, -122.2008187"),
                Location.forString("47.6776721, -122.2006229"),
                Location.forString("47.6775366, -122.2010923"),
                Location.forString("47.6776969, -122.2011787"),
                Location.forString("47.6778433, -122.2012807"));

        // Expected surface area obtained from JOSM
        Assert.assertEquals(1384.480, tennisCourt.surfaceOnSphere().asMeterSquared(), 2);

        // Try on a larger, more complex area (OSM way 23408944). Surface area from JOSM
        final Area forest = this.setup.getForestPolygon().area(23408944000000L);
        Assert.assertEquals(229960, forest.asPolygon().surfaceOnSphere().asMeterSquared(), 300);
    }

    @Test
    public void testTriangulate()
    {
        final Polygon polygon = Polygon.SILICON_VALLEY_2;
        final List<Polygon> triangles = polygon.triangles();
        Assert.assertEquals(3, triangles.size());
        for (final Polygon triangle : triangles)
        {
            Assert.assertEquals(3, triangle.size());
        }
    }
}

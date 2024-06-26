package org.openstreetmap.atlas.geography;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.locationtech.jts.io.WKBWriter;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author matthieun
 * @author mgostintsev
 * @author james-gage
 * @author hallahan
 */
public class PolygonTest
{
    /*
     * The .osm files associated with some of these tests describe some of the shapes used in tests
     * here, if the wkt's are updated in these tests the matching .osm files should also be updated.
     */

    private static final Logger logger = LoggerFactory.getLogger(PolygonTest.class);

    @Rule
    public final PolygonTestRule setup = new PolygonTestRule();

    private Polygon quadrant;
    private Rectangle rectangle;

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
    public void testAntimeridian()
    {
        final Polygon antimeridianWest = new Polygon(Location.forString("47.6778433, -180"),
                Location.forString("47.6779773, -180"), Location.forString("47.6776721, -180"),
                Location.forString("47.6775366, -179.9"), Location.forString("47.6776969, -179.9"),
                Location.forString("47.6778433, -179.9"));

        final Polygon antimeridianEast = new Polygon(Location.forString("47.6778433, 180"),
                Location.forString("47.6779773, 180"), Location.forString("47.6776721, 180"),
                Location.forString("47.6775366, 179.9"), Location.forString("47.6776969, 179.9"),
                Location.forString("47.6778433, 179.9"));

        Assert.assertFalse(antimeridianWest.intersects(antimeridianEast));

    }

    @Test
    public void testAsGeoJsonGeometry()
    {
        final Polygon tennisCourt = new Polygon(Location.forString("47.6778433, -122.2012807"),
                Location.forString("47.6779773, -122.2008187"),
                Location.forString("47.6776721, -122.2006229"),
                Location.forString("47.6775366, -122.2010923"),
                Location.forString("47.6776969, -122.2011787"),
                Location.forString("47.6778433, -122.2012807"));
        final String geoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[-122.2012807,47.6778433],[-122.2008187,47.6779773],[-122.2006229,47.6776721],[-122.2010923,47.6775366],[-122.2011787,47.6776969],[-122.2012807,47.6778433]]]}";
        final JsonObject geometry = tennisCourt.asGeoJsonGeometry();
        Assert.assertEquals(geoJson, geometry.toString());
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
        String polygonWkt = "POLYGON ((-70.0020146 12.5265405, -70.0019978 12.5265112, -70.0019564 12.526534, -70.0019733 12.5265633, -70.0020146 12.5265405))";
        Polygon polygon = Polygon.wkt(polygonWkt);
        Assert.assertFalse(polygon.isClockwise());
        Assert.assertTrue(polygon.reversed().isClockwise());

        polygonWkt = "POLYGON((70.5233941 -49.2953329,70.5234584 -49.2955148,70.5235872 -49.2963964,70.5230722 -49.2963964,70.5229649 -49.2962145,70.5227718 -49.2961585,70.5225143 -49.2960186,70.5224285 -49.2958507,70.5226216 -49.2955568,70.5223426 -49.2955288,70.5225358 -49.295095,70.5233941 -49.2953329))";
        polygon = Polygon.wkt(polygonWkt);
        Assert.assertTrue(polygon.isClockwise());
        Assert.assertFalse(polygon.reversed().isClockwise());

        polygonWkt = "POLYGON((69.5530556 -49.2504684,69.5530878 -49.2504019,69.5530985 -49.2503494,69.5531736 -49.2504089,69.5533614 -49.2504054,69.553415 -49.2504439,69.553356 -49.250584,69.5531629 -49.2505455,69.5530556 -49.2504684))";
        polygon = Polygon.wkt(polygonWkt);
        Assert.assertTrue(polygon.isClockwise());
        Assert.assertFalse(polygon.reversed().isClockwise());

        polygonWkt = "POLYGON((-58.7546961 -51.5537665,-58.754588 -51.5537003,-58.754411 -51.553737,-58.7543466 -51.5538204,-58.7543573 -51.5539338,-58.7544324 -51.5539838,-58.7545995 -51.5539833,-58.754706 -51.5539371,-58.7547543 -51.5538638,-58.7547597 -51.5538004,-58.7553229 -51.5538738,-58.7546961 -51.5537665))";
        polygon = Polygon.wkt(polygonWkt);
        Assert.assertTrue(polygon.isClockwise());
        Assert.assertFalse(polygon.reversed().isClockwise());

        polygonWkt = "POLYGON((72.3694563 -7.2629912,72.3701314 -7.2625735,72.3702161 -7.2625939,72.370768 -7.2634826,72.3707654 -7.263559,72.37008 -7.2640046,72.3700107 -7.2639715,72.369428 -7.2630548,72.3694563 -7.2629912))";
        polygon = Polygon.wkt(polygonWkt);
        Assert.assertTrue(polygon.isClockwise());
        Assert.assertFalse(polygon.reversed().isClockwise());
    }

    @Test
    public void testClosedLoop()
    {
        final Polygon polygon = Polygon.SILICON_VALLEY;
        final Polygon initialClosedLoop = new Polygon(polygon.closedLoop());
        Assert.assertNotEquals(
                "Last and Last - 1 locations for the polygon should not be duplicate.",
                initialClosedLoop.last(), initialClosedLoop.get(initialClosedLoop.size() - 2));
        final Polygon doubleClosedLoop = new Polygon(initialClosedLoop.closedLoop());
        Assert.assertNotEquals("Last and Last - 1 locations for polygon should not be duplicate.",
                doubleClosedLoop.last(), doubleClosedLoop.get(doubleClosedLoop.size() - 2));
    }

    @Test
    public void testComplexPolygon()
    {
        final Polygon polygon = Polygon
                .wkt("POLYGON ((0.0269943 0.0252426, 0.0441521 0.0453649, 0.0584354 0.0381783, "
                        + "0.0706524 0.0269494, 0.0770305 0.011678, 0.0716406 -0.0064678, 0.05062 -0.007995, "
                        + "0.0498115 -0.0120374, 0.0594235 -0.0167086, 0.0675982 -0.013295, 0.074066 -0.0169781, "
                        + "0.0688558 -0.0273986, 0.0557404 -0.0238053, 0.0534946 -0.0191341, 0.0469369 -0.0153611,"
                        + " 0.0464878 -0.0259613, 0.0557404 -0.029824, 0.0641846 -0.0317105, 0.060232 -0.0455445,"
                        + " 0.0462183 -0.0389868, 0.0313062 -0.0173374, 0.0269943 0.0252426))");
        final Location inside1 = Location.forString("-0.0209583, 0.0651123");
        final Location onBoundary1 = Location.forString("0.0301426, 0.0669962");
        final Location onBoundary2 = Location.forString("-0.0170728, 0.0317908");
        final Location onBoundary3 = Location.forString("0.0269494, 0.0706524");
        final Location outside1 = Location.forString(".0427412, .0722947");
        final Location outside2 = Location.forString("-0.022018, 0.0499233");
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(inside1));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(onBoundary1));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(onBoundary2));
        // see awt definition of contains
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(onBoundary3));
        Assert.assertFalse(polygon.fullyGeometricallyEncloses(outside1));
        Assert.assertFalse(polygon.fullyGeometricallyEncloses(outside2));
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
        Assert.assertTrue(concave.fullyGeometricallyEncloses(onBoundary2));
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
    public void testFourSelfIntersectingPolygonContains()
    {
        // Shape represents a square with an additional smaller square on each corner, formed by 4
        // self intersections
        final Polygon polygon = Polygon.wkt(
                "POLYGON ((-0.0380885 0.0238053, -0.0381783 0.0334173, -0.0255121 0.0337766, -0.0247036 -0.0263206, "
                        + "-0.0368309 -0.0268596, -0.03728 -0.0141933, 0.0270392 -0.0132052, 0.0273986 -0.0246138,"
                        + " 0.0171578 -0.0248833, 0.0153611 0.0342258, 0.0253324 0.0344953, 0.0257816 0.0256918, "
                        + "-0.0380885 0.0238053))");
        final Location upperLeft = Location.forString("0.0279376, -0.0300936");
        final Location upperRight = Location.forString("0.0297342, 0.0212002");
        final Location lowerLeft = Location.forString("-0.0203918, -0.0302732");
        final Location lowerRight = Location.forString("-0.0196731, 0.022782");
        final Location center = Location.forString("0.06378, -0.0053899");
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(upperLeft));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(upperRight));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(lowerLeft));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(lowerRight));
        // Interior square is considered exterior to the polygon
        Assert.assertFalse(polygon.fullyGeometricallyEncloses(center));
    }

    @Test
    public void testFullyGeometricallyEnclosesRectangleAndPolyLine()
    {
        final Polygon polygon3 = new Polygon(Location.forString("18.2226, -63.0508"),
                Location.forString("18.2209, -63.0437"), Location.forString("18.2117, -63.0484"),
                Location.forString("18.2139, -63.0563"), Location.forString("18.2226, -63.0508"));
        final Polygon polygon4 = Rectangle.forCorners(Location.forString("18.2112, -63.0568"),
                Location.forString("18.2232, -63.0432"));

        Assert.assertTrue(polygon4.fullyGeometricallyEncloses(polygon3.bounds()));
        Assert.assertTrue(polygon4.fullyGeometricallyEncloses(new Polygon(polygon3.closedLoop())));
        Assert.assertFalse(polygon3.fullyGeometricallyEncloses(polygon4.bounds()));
        Assert.assertFalse(polygon3.fullyGeometricallyEncloses(new Polygon(polygon4.closedLoop())));
    }

    @Test
    public void testFullyGeometricallyEnclosingMultiPolygon()
    {
        final MultiPolygon multiPolygon1 = MultiPolygon
                .wkt("MULTIPOLYGON (" + "((-10 10, 10 10, 10 -10, -10 -10, -10 10),"
                        + "(-6 6, 6 6, 6 -6, -6 -6, -6 6)))");
        final MultiPolygon multiPolygon2 = MultiPolygon.wkt("MULTIPOLYGON ("
                + "((-4 4, 4 4, 4 -4, -4 -4, -4 4)," + "(-3 3, 3 3, 3 -3, -3 -3, -3 3)))");
        final Polygon polygon1 = Polygon.wkt("POLYGON " + "((-8 8, 8 8, 8 -8, -8 -8, -8 8))");
        final Polygon polygon2 = Polygon.wkt("POLYGON " + "((-5 5, 5 5, 5 -5, -5 -5, -5 5))");

        Assert.assertFalse(polygon2.fullyGeometricallyEncloses(multiPolygon1));
        Assert.assertTrue(polygon1.fullyGeometricallyEncloses(multiPolygon2));
        Assert.assertFalse(polygon1.fullyGeometricallyEncloses(multiPolygon1));
        Assert.assertTrue(polygon2.fullyGeometricallyEncloses(multiPolygon2));
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
    public void testIsApproximatelyNSidedHalfCircle()
    {
        // Test half-circle like polygon (has 10 sides)
        // POLYGON ((-122.003467 37.324233, -122.003491 37.3242521, -122.003519 37.3242677,
        // -122.0035504 37.3242794, -122.0035845 37.324287, -122.0036207 37.32429, -122.0036583
        // 37.3242884, -122.0036964 37.3242819, -122.0037343 37.3242705, -122.0037712 37.3242542,
        // -122.0038063 37.324233, -122.003467 37.324233))
        final Polygon halfCircleLike = new Polygon(Location.STEVENS_CREEK,
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(315),
                        Distance.meters(3)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(310),
                        Distance.meters(6)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(305),
                        Distance.meters(9)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(300),
                        Distance.meters(12)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(295),
                        Distance.meters(15)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(290),
                        Distance.meters(18)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(285),
                        Distance.meters(21)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(280),
                        Distance.meters(24)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(275),
                        Distance.meters(27)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(270),
                        Distance.meters(30)));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(3, Angle.degrees(5)));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(3, Angle.degrees(10)));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(3, Angle.degrees(30)));
        Assert.assertTrue(halfCircleLike.isApproximatelyNSided(3, Angle.degrees(45)));
        Assert.assertFalse(halfCircleLike.isApproximatelyNSided(10, Angle.MINIMUM));
    }

    @Test
    public void testIsApproximatelyNSidedLine()
    {
        // Test a line (has no sides)
        // POLYGON ((-122.05576 37.332439, -121.955918 37.255731, -122.05576 37.332439, -122.05576
        // 37.332439))
        final Polygon polygon = new Polygon(Location.CROSSING_85_280, Location.CROSSING_85_17);
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(90)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MAXIMUM));
    }

    @Test
    public void testIsApproximatelyNSidedLineWithDuplicates()
    {
        // Test a line with duplicates (has no sides)
        // POLYGON ((-122.05576 37.332439, -121.955918 37.255731, -121.955918 37.255731, -122.05576
        // 37.332439))
        final Polygon polygon = new Polygon(Location.CROSSING_85_280, Location.CROSSING_85_17,
                Location.CROSSING_85_17);
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(90)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MAXIMUM));
    }

    @Test
    public void testIsApproximatelyNSidedPoint()
    {
        // Test point (has no sides)
        // POLYGON ((12.49234 41.890224, 12.49234 41.890224, 12.49234 41.890224, 12.49234
        // 41.890224))
        final Polygon polygon = new Polygon(Location.COLOSSEUM);
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(90)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MAXIMUM));
    }

    @Test
    public void testIsApproximatelyNSidedSelfIntersectingPolygon()
    {
        // Test a self-intersecting polygon
        // POLYGON ((-122.003467 37.324233, -122.05576 37.332439, -121.955918 37.255731, -122.003467
        // 37.324233, -122.0023361 37.324233, -122.003467 37.3251323, -122.003467 37.324233))
        final Polygon polygon = new Polygon(Location.STEVENS_CREEK, Location.CROSSING_85_280,
                Location.CROSSING_85_17, Location.STEVENS_CREEK,
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.EAST, Distance.meters(100)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.NORTH, Distance.meters(100)));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(90)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(4, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(5, Angle.MINIMUM));
        Assert.assertTrue(polygon.isApproximatelyNSided(6, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(6, Angle.MAXIMUM));
    }

    @Test
    public void testIsApproximatelyNSidedSquare()
    {
        // Test a square with ~90 degree heading changes
        // POLYGON ((-122.003467 37.324233, -122.0033539 37.324233, -122.0033539 37.3241431,
        // -122.003467 37.3241431, -122.003467 37.324233))
        final Polygon polygon = new Polygon(Location.STEVENS_CREEK,
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.EAST, Distance.meters(10)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.EAST, Distance.meters(10))
                        .shiftAlongGreatCircle(Heading.SOUTH, Distance.meters(10)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.SOUTH, Distance.meters(10)));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(30)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(60)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(89)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(89.99)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(90)));
        Assert.assertTrue(polygon.isApproximatelyNSided(4, Angle.MINIMUM));
        Assert.assertTrue(polygon.isApproximatelyNSided(4, Angle.degrees(1)));
        Assert.assertTrue(polygon.isApproximatelyNSided(4, Angle.degrees(89)));
        Assert.assertTrue(polygon.isApproximatelyNSided(4, Angle.degrees(89.99)));
        Assert.assertFalse(polygon.isApproximatelyNSided(4, Angle.degrees(90)));
        Assert.assertFalse(polygon.isApproximatelyNSided(5, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(5, Angle.MAXIMUM));
    }

    @Test
    public void testIsApproximatelyNSidedTriangle()
    {
        // Test a perfect triangle
        // POLYGON ((-122.05576 37.332439, -121.955918 37.255731, -122.003467 37.324233, -122.05576
        // 37.332439))
        final Polygon polygon = new Polygon(Location.CROSSING_85_280, Location.CROSSING_85_17,
                Location.STEVENS_CREEK);
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(30)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(45)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(60)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(90)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(4, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(4, Angle.MAXIMUM));
    }

    @Test
    public void testIsApproximatelyNSidedTriangleLike1()
    {
        // Test triangular like shape with one additional inner point
        // POLYGON ((-122.003467 37.324233, -122.003418 37.3242555, -122.0033691 37.324278,
        // -122.0035536 37.3242908, -122.003467 37.324233))
        final Polygon polygon = new Polygon(Location.STEVENS_CREEK,
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(60),
                        Distance.meters(5)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(60),
                        Distance.meters(10)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(310),
                        Distance.meters(10)));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(0.1)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(30)));
    }

    @Test
    public void testIsApproximatelyNSidedTriangleLike2()
    {
        // Test another triangular like shape with one additional inner point
        // POLYGON ((-122.05576 37.332439, -122.055711 37.3324615, -122.055662 37.332484,
        // -122.0558466 37.3324968, -122.05576 37.332439))
        final Polygon polygon = new Polygon(Location.STEVENS_CREEK,
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(59),
                        Distance.meters(5)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(60),
                        Distance.meters(10)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(310),
                        Distance.meters(10)));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(0.1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(0.5)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(2)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(3)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(30)));
    }

    @Test
    public void testIsApproximatelyNSidedTriangleLike3()
    {
        // Test another triangular like shape with several additional inner points
        // POLYGON ((-122.003467 37.324233, -122.0034571 37.3242374, -122.0033991 37.3242654,
        // -122.0033691 37.324278, -122.0035536 37.3242908, -122.0034922 37.3242511, -122.003467
        // 37.324233))
        final Polygon polygon = new Polygon(Location.STEVENS_CREEK,
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(61),
                        Distance.meters(1)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(59),
                        Distance.meters(7)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(60),
                        Distance.meters(10)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(310),
                        Distance.meters(10)),
                Location.STEVENS_CREEK.shiftAlongGreatCircle(Heading.degrees(312),
                        Distance.meters(3)));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(0.1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(0.5)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(2)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(3)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(30)));
    }

    @Test
    public void testIsApproximatelyNSidedTriangles()
    {
        // Test triangular generated by triangles method
        Polygon.SILICON_VALLEY_2.triangles().forEach(aTriangle ->
        {
            Assert.assertTrue(aTriangle.isApproximatelyNSided(3, Angle.MINIMUM));
            Assert.assertTrue(aTriangle.isApproximatelyNSided(3, Angle.degrees(1)));
            Assert.assertTrue(aTriangle.isApproximatelyNSided(3, Angle.degrees(30)));
        });
    }

    @Test
    public void testIsApproximatelyNSidedTrianglesWithDuplicates()
    {
        // Test a perfect triangle with duplicate inner points
        // POLYGON ((-122.05576 37.332439, -122.05576 37.332439, -121.955918 37.255731, -121.955918
        // 37.255731, -121.955918 37.255731, -122.003467 37.324233, -122.003467 37.324233,
        // -122.05576 37.332439))
        final Polygon polygon = new Polygon(Location.CROSSING_85_280, Location.CROSSING_85_280,
                Location.CROSSING_85_17, Location.CROSSING_85_17, Location.CROSSING_85_17,
                Location.STEVENS_CREEK, Location.STEVENS_CREEK);
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(1, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(2, Angle.MAXIMUM));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.MINIMUM));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(1)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(30)));
        Assert.assertTrue(polygon.isApproximatelyNSided(3, Angle.degrees(45)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(60)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.degrees(90)));
        Assert.assertFalse(polygon.isApproximatelyNSided(3, Angle.MAXIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(4, Angle.MINIMUM));
        Assert.assertFalse(polygon.isApproximatelyNSided(4, Angle.MAXIMUM));
    }

    @Test
    public void testMassivePolygonWithJTS()
    {
        final Polygon polygon = Polygon
                .wkt("POLYGON ((-160 -20, 179 -20, 179 -85, -160 -85, -160 -20))");
        final Location interior1 = Location.forString("-25,-150");
        final Location onBoundary1 = Location.forString("-20, -160");
        final Location onBoundary2 = Location.forString("-20, 179");
        final Location exterior1 = Location.forString("20, 0");
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(interior1));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(onBoundary1));
        // this would be excluded by awt definition of contains
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(onBoundary2));
        Assert.assertFalse(polygon.fullyGeometricallyEncloses(exterior1));
    }

    @Test
    public void testMaxWdithPolygonCausesAwtOverflow()
    {
        final Polygon maxWidthPolygon = Polygon.wkt(
                "POLYGON ((-180 -89.9999, -180 -62.8442818, 180 -62.8442818, 180 -89.9999, -180 -89.9999))");
        final Rectangle innerRectangle = Rectangle.forString("-80,-160:-70, 170");
        final Boolean encloses = maxWidthPolygon.fullyGeometricallyEncloses(innerRectangle);
        Assert.assertEquals(true, encloses);
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
        Assert.assertEquals(Location.forString("37.3352356,-122.0096687"), this.quadrant.middle());
    }

    @Test
    public void testPolygonWith45DegreeZeroAreaPartContains()
    {
        // Shape is a triangle, with a zero area line protruding from one of the corners on an
        // incline
        final Polygon polygon = Polygon.wkt("POLYGON ((-0.0065127 0.0214697, -0.0092975 0.0054797,"
                + " -0.0233112 -0.0085339, 0.0027398 0.0175171, -0.0065127 0.0214697))");
        final Location middleZeroAreaPart = polygon.segmentForIndex(1).middle();
        final Location endpointZeroAreaPart = polygon.segmentForIndex(1).end();
        final Location middleThirdSegment = polygon.segmentForIndex(2).middle();
        // Locations on the zero area part are still on the boundary, and therefore contained
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(middleZeroAreaPart));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(endpointZeroAreaPart));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(middleThirdSegment));
    }

    @Test
    public void testPolygonWithFlatZeroAreaPartContains()
    {
        // Shape is a triangle, with a zero area line protruding from one of the corners as if
        // extending from the base
        final Polygon polygon = Polygon.wkt("POLYGON ((-0.0000449 0.0091179, -0.0063331 0.0007635, "
                + "-0.0174722 0.0007635, 0.0135196 0.0007635, -0.0000449 0.0091179))");
        final Location onZeroAreaPart = Location.forString("0.0007635, 0.0132954");
        final Location endpoint = Location.forString("0.0007635,-0.0174722");
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(onZeroAreaPart));
        // see awt definition of contains
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(endpoint));
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
    public void testSelfIntersectingPolygon()
    {
        // shape is a figure 8, self intersecting in the middle
        final Polygon polygon = Polygon.wkt(
                "POLYGON ((-0.0256918 0.0054797, -0.0220985 0.0120374, -0.0119475 0.0121272, -0.0054797 0.006917,"
                        + " -0.007995 -0.0128459, -0.0007186 -0.0194934, 0.0103306 -0.0186849, 0.0125764 -0.0098814, "
                        + "0.0026051 -0.0048509, -0.013834 -0.0050305, -0.0242545 -0.0008983, -0.0256918 0.0054797))");
        final Location area1 = Location.forString("0.0034136, -0.016529");
        final Location area2 = Location.forString("-0.0126662, 0.0026051");
        final Location outside1 = Location.forString("0.0007187, 0.0029644");
        final Location boundary1 = Location.forString("0.0120374, -0.0220985");
        // both areas formed by the self intersection are contained
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(area1));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(area2));
        Assert.assertFalse(polygon.fullyGeometricallyEncloses(outside1));
        Assert.assertTrue(polygon.fullyGeometricallyEncloses(boundary1));
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
    public void testToWkb()
    {
        final Polygon polygon = Polygon.wkt(
                "POLYGON((-22.5092778 65.0717446,-22.5092214 65.0717039,-22.5091195 65.0716869,-22.509031 65.0716847,-22.5089264 65.071705,-22.5088164 65.0717638,-22.5085884 65.0717785,-22.508449 65.0717593,-22.5082612 65.0717378,-22.5079903 65.0717367,-22.5078401 65.0717333,-22.5074565 65.0717875,-22.5072849 65.0718531,-22.5071454 65.0719017,-22.5070113 65.07193,-22.5069094 65.0719797,-22.5067806 65.0720103,-22.5066465 65.0720487,-22.5065419 65.0720894,-22.5065741 65.0721583,-22.506676 65.0721697,-22.5068691 65.0722341,-22.5071883 65.0722488,-22.5075585 65.0722307,-22.5078964 65.0721923,-22.5081754 65.0721493,-22.508508 65.0720905,-22.5088862 65.0720091,-22.509023 65.0719277,-22.5091329 65.0719029,-22.5091544 65.0718644,-22.5091705 65.0718102,-22.5092214 65.0717751,-22.5092778 65.0717446))");
        final String validPolygonWkb = "00000000030000000100000022C036826007A7942E4050449776A9AA89C036825C556B6C6040504496CBF45FB9C0368255A7D241814050449684A6C34EC036824FDB09A672405044967B6C87E9C03682490024122E40504496D0917D6BC0368241CAA5AB2040504497C7318259C0368232D9711FFE4050449804D98394C0368229B6B2AF1440504497B451ABC4C036821D67EFD362405044975A2438C4C036820BA6FD2F6B4050449755871B12C0368201CF0D18ED4050449747446230C03681E8AB4FA483405044982A9930BEC03681DD6C57412F405044993DBEA770C03681D447EB511C4050449A099681B7C03681CB7E1833D34050449A8049667BC03681C4D07F08F54050449B50BE5E75C03681BC5F973F4C4050449BD116DE69C03681B395C422034050449C72268E09C03681ACBADE8DBF4050449D1CDBD8D9C03681AED718802A4050449E3DD8A8A3C03681B584B1AB084050449E6DA950C7C03681C22C5FDA5B4050449F7BC649FCC03681D717A969F14050449FB96E4B37C03681EF5A964E8B4050449F6D83911AC03682057F9BC1924050449ECC73E179C0368217C873A1B84050449E1818FB7AC036822D948DC11E4050449D2178F68CC03682465DB262BE4050449BCC0E60ECC036824F54D1E96C4050449A76A3CB4CC036825688A2D1514050449A0E9EFF34C0368257F1589D50405044996D23EFC9C0368258FF7596854050449889CF213BC036825C556B6C6040504497F696CAB2C036826007A7942E4050449776A9AA89";
        Assert.assertEquals(validPolygonWkb, WKBWriter.toHex(polygon.toWkb()));
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

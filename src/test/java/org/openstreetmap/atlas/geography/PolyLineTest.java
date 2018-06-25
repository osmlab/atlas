package org.openstreetmap.atlas.geography;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.converters.WktPolyLineConverter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class PolyLineTest
{
    private static final Logger logger = LoggerFactory.getLogger(PolyLineTest.class);

    @Test
    public void testAppend()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final PolyLine line2 = new PolyLine(Location.TEST_1, Location.TEST_7);
        final PolyLine appended = line.append(line2);
        Assert.assertTrue(appended.equalsShape(
                new PolyLine(Location.CROSSING_85_280, Location.TEST_1, Location.TEST_7)));
    }

    @Test
    public void testContains()
    {
        final PolyLine line = PolyLine
                .wkt("LINESTRING (10.5553105 48.3419094, 10.5552096 48.3417501, 10.5551312 48.3416583, "
                        + "10.5551027 48.341611, 10.5550183 48.3415143, 10.5549357 48.3414668, "
                        + "10.5548325 48.3414164, 10.5548105 48.3415201, 10.5548015 48.3415686, "
                        + "10.5548925 48.3416166, 10.5550334 48.3416375, 10.5551312 48.3416583)");
        Assert.assertTrue("Verifying PolyLine contains Location",
                line.contains(Location.forString("48.3419094, 10.5553105")));
        Assert.assertFalse("Verifying PolyLine doesn't contain Location",
                line.contains(Location.COLOSSEUM));
        Assert.assertTrue("Verifying PolyLine contains Segment",
                line.contains(new Segment(Location.forString("48.3419094, 10.5553105"),
                        Location.forString("48.3417501, 10.5552096"))));
        Assert.assertFalse("Verifying PolyLine doesn't contain Segment",
                line.contains(new Segment(Location.TEST_2, Location.COLOSSEUM)));
    }

    @Test
    public void testDistanceCost()
    {
        final PolyLine source = new PolyLine(Location.CROSSING_85_280, Location.TEST_2,
                Location.TEST_1);
        final PolyLine destination = new PolyLine(Location.CROSSING_85_280, Location.TEST_6,
                Location.TEST_1);
        final Distance distanceToOneWay = source.averageOneWayDistanceTo(destination);
        final Distance distanceFromOneWay = destination.averageOneWayDistanceTo(source);
        final Distance distanceTo = source.averageDistanceTo(destination);
        final Distance distanceFrom = destination.averageDistanceTo(source);
        logger.info("distanceToOneWay: {}", distanceToOneWay);
        logger.info("distanceFromOneWay: {}", distanceFromOneWay);
        logger.info("distanceTo: {}", distanceTo);
        logger.info("distanceFrom: {}", distanceFrom);
        Assert.assertEquals(distanceTo, distanceFrom);
        Assert.assertTrue(
                Location.TEST_2.distanceTo(Location.TEST_6).asMeters() / 3 > distanceTo.asMeters());
    }

    @Test
    public void testEqualsShape()
    {
        final PolyLine polyLine1 = PolyLine
                .wkt("LINESTRING (10.5553105 48.3419094, 10.5552096 48.3417501, 10.5551312 48.3416583, "
                        + "10.5551027 48.341611, 10.5550183 48.3415143, 10.5549357 48.3414668, "
                        + "10.5548325 48.3414164, 10.5548105 48.3415201, 10.5548015 48.3415686, "
                        + "10.5548925 48.3416166, 10.5550334 48.3416375, 10.5551312 48.3416583)");
        final PolyLine polyLine2 = PolyLine
                .wkt("LINESTRING (10.5551312 48.3416583, 10.5551027 48.341611, 10.5550183 48.3415143, "
                        + "10.5549357 48.3414668, 10.5548325 48.3414164, 10.5548105 48.3415201, "
                        + "10.5548015 48.3415686, 10.5548925 48.3416166, 10.5550334 48.3416375, "
                        + "10.5551312 48.3416583, 10.5552096 48.3417501, 10.5553105 48.3419094)");
        Assert.assertFalse(polyLine1.equals(polyLine2));
        Assert.assertTrue(polyLine1.equalsShape(polyLine2));
    }

    @Test
    public void testInnerLocations()
    {
        final PolyLine source = new PolyLine(Location.CROSSING_85_280, Location.TEST_7,
                Location.TEST_1);
        Assert.assertTrue("Make sure there is a single inner location",
                source.innerLocations().iterator().next().equals(Location.TEST_7));
    }

    @Test(expected = CoreException.class)
    public void testInvalidAppend()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final PolyLine line2 = new PolyLine(Location.CROSSING_85_280, Location.TEST_7);
        @SuppressWarnings("unused")
        final PolyLine appended = line.append(line2);
    }

    @Test(expected = CoreException.class)
    public void testInvalidPrepend()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final PolyLine line2 = new PolyLine(Location.CROSSING_85_280, Location.TEST_7);
        @SuppressWarnings("unused")
        final PolyLine prepended = line.prepend(line2);
    }

    @Test
    public void testNoInnerLocations()
    {
        final PolyLine source = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        Assert.assertTrue("Make sure the iterable is empty",
                Iterables.isEmpty(source.innerLocations()));
    }

    @Test
    public void testOverlapsShape()
    {
        final PolyLine larger = new PolyLine(Location.CROSSING_85_280, Location.TEST_1,
                Location.TEST_7);
        final PolyLine smaller = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final PolyLine smallerReversed = new PolyLine(Location.TEST_1, Location.CROSSING_85_280);

        Assert.assertTrue(larger.overlapsShapeOf(smaller));
        Assert.assertTrue(larger.overlapsShapeOf(smallerReversed));
        Assert.assertTrue(smaller.overlapsShapeOf(smallerReversed));
        Assert.assertTrue(smallerReversed.overlapsShapeOf(smaller));

        Assert.assertFalse(smaller.overlapsShapeOf(larger));
        Assert.assertFalse(smallerReversed.overlapsShapeOf(larger));
    }

    @Test
    public void testPrepend()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final PolyLine line2 = new PolyLine(Location.TEST_7, Location.CROSSING_85_280);
        final PolyLine prepended = line.prepend(line2);
        Assert.assertTrue(prepended.equalsShape(
                new PolyLine(Location.TEST_7, Location.CROSSING_85_280, Location.TEST_1)));
    }

    @Test
    public void testSelfIntersects()
    {
        final PolyLine polyLine = new WktPolyLineConverter().backwardConvert(
                "LINESTRING(-122.0095413 37.3362091,-122.0095716 37.3353178,-122.009566 37.33531,-122.0095604 37.3353178,-122.0095907 37.3362091)");
        Assert.assertTrue(polyLine.selfIntersects());
        Assert.assertEquals(1, polyLine.selfIntersections().size());
    }

    @Test
    public void testSelfIntersectsClosedLoop()
    {
        final PolyLine polyLine = PolyLine.wkt("LINESTRING(1 1, 2 2, 3 3, 3 1, 1 1)");
        Assert.assertTrue(polyLine.selfIntersects());
        final Set<Location> intersections = polyLine.selfIntersections();
        Assert.assertEquals(1, intersections.size());
        Assert.assertEquals(Location.forString("1,1"), intersections.toArray()[0]);
    }

    @Test
    public void testSelfIntersectsNoIntersections()
    {
        final PolyLine polyLine = PolyLine.wkt("LINESTRING(1 1, 2 2, 3 3, 3 1)");
        Assert.assertFalse(polyLine.selfIntersects());
        Assert.assertEquals(0, polyLine.selfIntersections().size());
    }

    @Test
    public void testSelfIntersectsPolygonNoIntersections()
    {
        final Polygon polygon = Polygon.wkt("POLYGON ((1 1, 2 2, 3 3, 1 3, 1 1))");
        Assert.assertFalse(polygon.selfIntersects());
        Assert.assertEquals(0, polygon.selfIntersections().size());
    }

    @Test
    public void testSelfIntersectsTouchFirst()
    {
        final PolyLine polyLine = PolyLine.wkt("LINESTRING(2 1, 3 2, 3 1, 1 1)");
        Assert.assertTrue(polyLine.selfIntersects());
        final Set<Location> intersections = polyLine.selfIntersections();
        Assert.assertEquals(1, intersections.size());
        Assert.assertEquals(Location.forString("1,2"), intersections.toArray()[0]);
    }

    @Test
    public void testSelfIntersectsTouchLast()
    {
        final PolyLine polyLine = PolyLine.wkt("LINESTRING(1 1, 3 1, 3 2, 2 1)");
        Assert.assertTrue(polyLine.selfIntersects());
        final Set<Location> intersections = polyLine.selfIntersections();
        Assert.assertEquals(1, intersections.size());
        Assert.assertEquals(Location.forString("1,2"), intersections.toArray()[0]);
    }

    @Test
    public void testSelfIntersectsTouchMiddle()
    {
        final PolyLine polyLine = PolyLine.wkt("LINESTRING(1 1, 3 1, 3 2, 2 1, 1 2)");
        Assert.assertTrue(polyLine.selfIntersects());
        final Set<Location> intersections = polyLine.selfIntersections();
        Assert.assertEquals(1, intersections.size());
        Assert.assertEquals(Location.forString("1,2"), intersections.toArray()[0]);
    }

    @Test
    public void testToString()
    {
        final PolyLine singleLocationPolyLine = new PolyLine(Location.CROSSING_85_280);
        final PolyLine multipleLocationPolyLine = new PolyLine(Location.CROSSING_85_280,
                Location.TEST_1);

        Assert.assertEquals("POINT (-122.05576 37.332439)", singleLocationPolyLine.toString());
        Assert.assertEquals("LINESTRING (-122.05576 37.332439, -122.009566 37.33531)",
                multipleLocationPolyLine.toString());
    }

    @Test
    public void testTruncatingFromBothSides()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1,
                Location.TEST_2);
        final Iterable<Location> truncated = line.truncate(1, 1);
        Assert.assertTrue(new PolyLine(truncated).equals(new PolyLine(Location.TEST_1)));
    }

    @Test
    public void testTruncatingFromEnd()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final Iterable<Location> truncated = line.truncate(0, 1);
        Assert.assertTrue(new PolyLine(truncated).equals(new PolyLine(Location.CROSSING_85_280)));
    }

    @Test
    public void testTruncatingFromStart()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final Iterable<Location> truncated = line.truncate(1, 0);
        Assert.assertTrue(new PolyLine(truncated).equals(new PolyLine(Location.TEST_1)));
    }

    @Test
    public void testTruncatingWithEndIndexEqualToPolyLineSize()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final Iterable<Location> truncated = line.truncate(0, 2);
        Assert.assertTrue("Make sure truncated is empty", Iterables.isEmpty(truncated));
    }

    @Test
    public void testTruncatingWithInvalidIndex()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final Iterable<Location> truncated = line.truncate(-1, 1);
        Assert.assertTrue("Make sure truncated is empty", Iterables.isEmpty(truncated));
    }

    @Test
    public void testTruncatingWithStartAndEndCombinedEqualToPolyLineSize()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final Iterable<Location> truncated = line.truncate(1, 1);
        Assert.assertTrue("Make sure truncated is empty", Iterables.isEmpty(truncated));
    }

    @Test
    public void testTruncatingWithStartIndexEqualToPolyLineSize()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final Iterable<Location> truncated = line.truncate(2, 0);
        Assert.assertTrue("Make sure truncated is empty", Iterables.isEmpty(truncated));
    }

    @Test
    public void testTruncatingWithZeroIndices()
    {
        final PolyLine line = new PolyLine(Location.CROSSING_85_280, Location.TEST_1);
        final Iterable<Location> truncated = line.truncate(0, 0);
        Assert.assertTrue(new PolyLine(truncated).equals(line));
    }

    @Test
    public void testWithoutDuplicateConsecutiveShapePoints()
    {
        final PolyLine polyLine1 = PolyLine.wkt(
                "LINESTRING (-75.616326 40.194564, -75.616330 40.194570, -75.616330 40.194570, -75.616340 40.194580)");
        final PolyLine polyLine2 = PolyLine.wkt(
                "LINESTRING (-75.616326 40.194564, -75.616330 40.194570, -75.616340 40.194580)");
        final PolyLine polyLine3 = PolyLine.wkt(
                "LINESTRING (-75.616326 40.194564, -75.616330 40.194570, -75.616340 40.194580, -75.616330 40.194570)");
        final PolyLine polyLine4 = PolyLine.wkt(
                "LINESTRING (-75.616330 40.194570, -75.616330 40.194570, -75.616330 40.194570, -75.616340 40.194580, -75.616340 40.194590)");
        final PolyLine polyLine5 = PolyLine.wkt(
                "LINESTRING (-75.616330 40.194570, -75.616340 40.194580, -75.616340 40.194590)");
        Assert.assertEquals(polyLine2.toWkt(),
                polyLine1.withoutDuplicateConsecutiveShapePoints().toWkt());
        Assert.assertEquals(polyLine3.toWkt(),
                polyLine3.withoutDuplicateConsecutiveShapePoints().toWkt());
        Assert.assertEquals(polyLine5.toWkt(),
                polyLine4.withoutDuplicateConsecutiveShapePoints().toWkt());
    }

}

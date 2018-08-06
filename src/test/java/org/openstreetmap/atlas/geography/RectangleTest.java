package org.openstreetmap.atlas.geography;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Surface;

/**
 * @author matthieun
 */
public class RectangleTest
{
    private final Location location1 = Location.TEST_6;
    private final Location location2 = Location.TEST_4;
    private final Location location3 = Location.TEST_1;
    private final Location location4 = Location.TEST_2;

    private final Rectangle rectangle1 = Rectangle.forLocations(this.location1, this.location2);
    private final Rectangle rectangle2 = Rectangle.forLocations(this.location2, this.location3);
    private final Rectangle rectangle3 = Rectangle.forLocations(this.location1, this.location3);
    private final Rectangle rectangle4 = Rectangle.forLocations(this.location2, this.location4);

    @Test
    public void testCoversAndCoversPartially()
    {
        Assert.assertTrue("Rectangle 3 fully contains rectangle 2",
                this.rectangle3.fullyGeometricallyEncloses(this.rectangle2));
        Assert.assertTrue("That means, it should also cover it partially",
                this.rectangle3.overlaps(this.rectangle4));

        Assert.assertTrue("Rectangle 3 only partially covers rectangle 4",
                this.rectangle3.overlaps(this.rectangle4));
        Assert.assertFalse("It should not fully contain it",
                this.rectangle3.fullyGeometricallyEncloses(this.rectangle4));

        Assert.assertTrue("Rectangle 2 only partly overlaps rectangle 4",
                this.rectangle2.overlaps(this.rectangle4));
        Assert.assertFalse("But does not fully contain it",
                this.rectangle2.fullyGeometricallyEncloses(this.rectangle4));
    }

    @Test
    public void testContract()
    {
        // Test over contract
        final Distance rectangle1CornerToCorner = this.rectangle1.lowerLeft()
                .distanceTo(this.rectangle1.upperRight());
        final Rectangle collapsedRectangle1 = this.rectangle1.contract(rectangle1CornerToCorner);
        Assert.assertEquals(collapsedRectangle1.center().bounds(), collapsedRectangle1);
        Assert.assertEquals(Surface.forDm7Squared(0), collapsedRectangle1.surface());

        // test compatibility with expand
        final Rectangle expanded = this.rectangle1.expand(Distance.ONE_METER);
        Assert.assertEquals(this.rectangle1, expanded.contract(Distance.ONE_METER));

        // test collapse horizontally
        final Location rectangle1UpperLeft = Iterables.asList(this.rectangle1).get(1);
        final Distance contractRectangle1Distance = rectangle1UpperLeft
                .distanceTo(this.rectangle1.upperRight()).scaleBy(.51);
        final Rectangle collapsedHorizontally = this.rectangle1
                .contract(contractRectangle1Distance);
        Assert.assertEquals(Surface.forDm7Squared(0), collapsedHorizontally.surface());
        Assert.assertEquals(collapsedHorizontally.surface(),
                this.rectangle1.contract(contractRectangle1Distance.scaleBy(10)).surface());
        Assert.assertEquals(Math.round(contractRectangle1Distance.asMeters()),
                Math.round(new Location(this.rectangle1.lowerLeft().getLatitude(),
                        collapsedHorizontally.middle().getLongitude())
                                .distanceTo(collapsedHorizontally.lowerLeft()).asMeters()));

        // test collapse vertically
        final Location rectangle2UpperLeft = Iterables.asList(this.rectangle2).get(1);
        final Distance contractRectangle2Distance = rectangle2UpperLeft
                .distanceTo(this.rectangle2.lowerLeft()).scaleBy(.51);
        final Rectangle collapsedVertically = this.rectangle2.contract(contractRectangle2Distance);
        Assert.assertEquals(Surface.forDm7Squared(0), collapsedVertically.surface());
        Assert.assertEquals(collapsedVertically.surface(),
                this.rectangle2.contract(contractRectangle2Distance.scaleBy(10)).surface());
        Assert.assertEquals(Math.round(contractRectangle2Distance.asMeters()),
                Math.round(new Location(collapsedVertically.middle().getLatitude(),
                        this.rectangle2.lowerLeft().getLongitude())
                                .distanceTo(collapsedVertically.lowerLeft()).asMeters()));

        // Test fully collapse Collapsed
        Assert.assertEquals(this.rectangle1.center().bounds(),
                collapsedHorizontally.contract(Distance.TEN_MILES));
        Assert.assertEquals(this.rectangle2.center().bounds(),
                collapsedVertically.contract(Distance.TEN_MILES));
    }

    @Test
    public void testIntersection()
    {
        Assert.assertEquals(Rectangle.forLocations(this.location2),
                this.rectangle1.intersection(this.rectangle2));
        Assert.assertEquals(this.rectangle1, this.rectangle1.intersection(this.rectangle3));
        Assert.assertEquals(
                Rectangle.forLocations(Location.TEST_4,
                        new Location(Location.TEST_6.getLatitude(),
                                Location.TEST_2.getLongitude())),
                this.rectangle3.intersection(this.rectangle4));
    }

    @Test
    public void testIntersectsAndCoversWithPolyLine()
    {
        final PolyLine polyLine = new PolyLine(this.location1, this.location2);

        Assert.assertTrue("Larger rectangle intersects polyline",
                this.rectangle4.intersects(polyLine));
        Assert.assertTrue("Smaller rectangle intersects polyline",
                this.rectangle3.intersects(polyLine));

        Assert.assertTrue("Smaller rectangle only touches the polyline, but does not cover it",
                this.rectangle3.fullyGeometricallyEncloses(polyLine));
        Assert.assertFalse("Larger rectangle fully contains the polyline",
                this.rectangle4.fullyGeometricallyEncloses(polyLine));
    }

    @Test
    public void testSurface()
    {
        final Surface surface = this.rectangle3.intersection(this.rectangle4).surface();
        Assert.assertTrue(surface.isLessThan(this.rectangle3.surface()));
        Assert.assertTrue(this.rectangle1.surface().add(this.rectangle2.surface())
                .isLessThan(this.rectangle3.surface()));
    }

    @Test
    public void testAntiMeridianEastRectangle()
    {
        final Location antiMeridian = new Location(Latitude.ZERO, Longitude.degrees(180));
        final Location lowerLeftAntiMeridianRectangle = new Location(Latitude.degrees(-10),
                Longitude.degrees(170));
        final Location lowerLeftTestRectangle = new Location(Latitude.degrees(-10),
                Longitude.degrees(150));
        final Location upperRightTestRectangle1 = new Location(Latitude.ZERO,
                Longitude.degrees(160));
        final Location upperRightTestRectangle2 = new Location(Latitude.ZERO,
                Longitude.degrees(175));

        // List construction
        final Rectangle antiMeridianRectangle1 = Rectangle
                .forLocations(Arrays.asList(antiMeridian, lowerLeftAntiMeridianRectangle));
        final Rectangle testRectangle1 = Rectangle
                .forLocations(Arrays.asList(upperRightTestRectangle1, lowerLeftTestRectangle));
        Assert.assertFalse(testRectangle1.overlaps(antiMeridianRectangle1));
        Assert.assertFalse(antiMeridianRectangle1.overlaps(testRectangle1));

        // Corners construction
        final Rectangle antiMeridianRectangle2 = Rectangle
                .forCorners(lowerLeftAntiMeridianRectangle, antiMeridian);
        final Rectangle testRectangle2 = Rectangle.forCorners(lowerLeftTestRectangle,
                upperRightTestRectangle2);
        Assert.assertTrue(testRectangle2.overlaps(antiMeridianRectangle2));
        Assert.assertTrue(antiMeridianRectangle2.overlaps(testRectangle2));
    }
}

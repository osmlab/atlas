package org.openstreetmap.atlas.geography;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Surface;

import com.google.gson.JsonArray;

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

    @Test
    public void testAsGeoJsonBbox()
    {
        final Rectangle rectangle = Rectangle.TEST_RECTANGLE;
        final JsonArray array = rectangle.asGeoJsonBbox();
        Assert.assertEquals("[-122.031905,37.328167,-122.029051,37.330394]", array.toString());
    }

    @Test(expected = CoreException.class)
    public void testConstructInvalidRectangle()
    {
        // The lower left is actually the lower right and the upper right is actually the upper
        // left, making this an invalid specification.
        @SuppressWarnings("unused")
        final Rectangle invalidRectangle = Rectangle.forCorners(
                Location.forWkt("POINT (-122.288925 47.618916)"),
                Location.forWkt("POINT (-122.288935 47.618946)"));
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
    public void testCoversAndCoversPartially()
    {
        Assert.assertTrue("Rectangle 3 fully contains rectangle 2",
                this.rectangle3.fullyGeometricallyEncloses(this.rectangle2));
        Assert.assertTrue("That means, it should also cover it partially",
                this.rectangle3.overlaps(this.rectangle2));

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
    public void testExpansionAcrossMeridians()
    {
        final long kilometersPerDegreeLongitudeNearNorthPole = Math.round(Distance
                .distancePerDegreeLongitudeAt(Location.forWkt("POINT(178 87)")).asKilometers());
        final long kilometersPerDegreeLongitudeNearSouthPole = Math.round(Distance
                .distancePerDegreeLongitudeAt(Location.forWkt("POINT(-178 87)")).asKilometers());

        Rectangle closeToNorthPole = Rectangle.forCorners(Location.forWkt("POINT(178 84)"),
                Location.forWkt("POINT(179 87)"));
        closeToNorthPole = closeToNorthPole.expandHorizontally(
                Distance.kilometers(kilometersPerDegreeLongitudeNearNorthPole * 100));
        Assert.assertEquals(
                "POLYGON ((135.8977545 81.937154, 135.8977545 83.8283367, 179.9999999 83.8283367, 179.9999999 81.937154, 135.8977545 81.937154))",
                closeToNorthPole.toWkt());

        Rectangle closeToSouthPole = Rectangle.forCorners(Location.forWkt("POINT(-179 84)"),
                Location.forWkt("POINT(-178 87)"));
        closeToSouthPole = closeToSouthPole.expandHorizontally(
                Distance.kilometers(kilometersPerDegreeLongitudeNearSouthPole * 100));
        Assert.assertEquals(
                "POLYGON ((-180 81.937154, -180 83.8283367, -116.9898158 83.8283367, -116.9898158 81.937154, -180 81.937154))",
                closeToSouthPole.toWkt());
    }

    @Test
    public void testExpansionAcrossPoles()
    {
        final long kilometersPerDegreeLatitude = Math
                .round(Distance.APPROXIMATE_DISTANCE_PER_DEGREE_AT_EQUATOR.asKilometers());

        Rectangle closeToSouthPole = Rectangle.forCorners(Location.forWkt("POINT(0 -87)"),
                Location.forWkt("POINT(1 -84)"));
        // this should cross the south pole
        closeToSouthPole = closeToSouthPole
                .expand(Distance.kilometers(kilometersPerDegreeLatitude * 4));
        Assert.assertEquals(
                "POLYGON ((-89.9998709 -86.0070121, -89.9998709 -79.2463163, 22.9131203 -79.2463163, 22.9131203 -86.0070121, -89.9998709 -86.0070121))",
                closeToSouthPole.toWkt());

        Rectangle closeToNorthPole = Rectangle.forCorners(Location.forWkt("POINT(0 84)"),
                Location.forWkt("POINT(1 87)"));
        // this should cross the north pole
        closeToNorthPole = closeToNorthPole
                .expand(Distance.kilometers(kilometersPerDegreeLatitude * 4));
        Assert.assertEquals(
                "POLYGON ((-21.9131203 79.2463163, -21.9131203 86.0070121, 90.9998709 86.0070121, 90.9998709 79.2463163, -21.9131203 79.2463163))",
                closeToNorthPole.toWkt());
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

        Assert.assertEquals(6479999998200000000L, Rectangle.MAXIMUM.surface().asDm7Squared());
    }

    @Test
    public void testWidth()
    {
        Assert.assertEquals(50160, this.rectangle1.width().asDm7());
        Assert.assertEquals(-1, Rectangle.MAXIMUM.width().asDm7());
    }
}

package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Test;
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
}

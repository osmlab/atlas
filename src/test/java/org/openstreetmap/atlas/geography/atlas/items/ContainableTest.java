package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * @author Yazad Khambata
 */
public class ContainableTest
{

    public static final int NUMBER_POINTS = 5;

    @Rule
    public ContainableTestRule containableTestRule = new ContainableTestRule();

    @Test
    public void testLocationIsWithin()
    {
        final GeometricSurface surface = rectOne();

        final Location center = ((Rectangle) surface).center();

        Assert.assertTrue(center.within(surface));

        Assert.assertTrue(((Rectangle) surface).lowerLeft().within(surface));
        Assert.assertTrue(((Rectangle) surface).upperRight().within(surface));

        Assert.assertFalse(
                new Location(Latitude.degrees(50), Longitude.degrees(50)).within(surface));
    }

    @Test
    public void testPolyLineIsWithin()
    {
        final GeometricSurface surface1 = rectOne();

        final GeometricSurface surface2 = rectTwo();

        final PolyLine polyLine1 = PolyLine.random(NUMBER_POINTS, (Rectangle) surface1);

        final PolyLine polyLine2 = PolyLine.random(NUMBER_POINTS, (Rectangle) surface2);

        Assert.assertTrue(polyLine1.within(surface1));

        Assert.assertTrue(polyLine2.within(surface2));

        Assert.assertFalse(polyLine1.within(surface2));
        Assert.assertFalse(polyLine2.within(surface1));
    }

    @Test
    public void testPolygonIsWithin()
    {
        final GeometricSurface surface1 = rectOne();
        final GeometricSurface surface2 = rectTwo();

        final Polygon polygon1 = Polygon.random(NUMBER_POINTS, (Rectangle) surface1);
        final Polygon polygon2 = Polygon.random(NUMBER_POINTS, (Rectangle) surface2);

        Assert.assertTrue(polygon1.within(surface1));
        Assert.assertTrue(polygon2.within(surface2));

        Assert.assertFalse(polygon1.within(surface2));
        Assert.assertFalse(polygon2.within(surface1));
    }

    @Test
    public void testAtlasEntities()
    {
        final long identifier = ContainableTestRule.ID;

        final Atlas atlas = this.containableTestRule.getAtlas();

        Assert.assertTrue(atlas.node(identifier).within(rectThree()));
        Assert.assertTrue(atlas.edge(identifier).within(rectThree()));
        Assert.assertTrue(atlas.area(identifier).within(rectThree()));
        Assert.assertTrue(atlas.relation(identifier).within(rectThree()));

        Assert.assertFalse(atlas.node(identifier).within(rectTwo()));
        Assert.assertFalse(atlas.edge(identifier).within(rectTwo()));
        Assert.assertFalse(atlas.area(identifier).within(rectTwo()));
        Assert.assertFalse(atlas.relation(identifier).within(rectTwo()));
    }

    private GeometricSurface rectOne()
    {
        final Location lowerLeft = new Location(Latitude.degrees(10), Longitude.degrees(10));
        final Location upperRight = new Location(Latitude.degrees(20), Longitude.degrees(20));

        return Rectangle.forCorners(lowerLeft, upperRight);
    }

    private GeometricSurface rectTwo()
    {
        final Location lowerLeft2 = new Location(Latitude.degrees(-20), Longitude.degrees(-20));
        final Location upperRight2 = new Location(Latitude.degrees(-10), Longitude.degrees(-10));

        return Rectangle.forCorners(lowerLeft2, upperRight2);
    }

    private GeometricSurface rectThree()
    {
        final Location lowerLeft = new Location(Latitude.degrees(10), Longitude.degrees(100));
        final Location upperRight = new Location(Latitude.degrees(20), Longitude.degrees(150));

        return Rectangle.forCorners(lowerLeft, upperRight);
    }

}

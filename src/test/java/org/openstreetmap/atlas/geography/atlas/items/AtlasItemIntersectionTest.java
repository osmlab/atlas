package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Tests atlas item intersection with {@link Polygon} functionality.
 *
 * @author mgostintsev
 */
public class AtlasItemIntersectionTest
{
    private static final String WITHIN_TEST_POLYGON_WKT = "POLYGON ((-122.2886447 47.6182798, -122.2886447 47.618416, -122.289001 47.618416, -122.289001 47.6182798, -122.2886447 47.6182798))";

    @Rule
    public final AtlasItemIntersectionTestRule rule = new AtlasItemIntersectionTestRule();

    @Test
    public void testAreasIntersectingPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT(-122.288935 47.618916)"),
                Location.forWkt("POINT(-122.288925 47.618946)"));

        final Rectangle fullyInside = Rectangle.forCorners(
                Location.forWkt("POINT(-122.288798 47.618472)"),
                Location.forWkt("POINT(-122.288788 47.618482)"));

        final Rectangle touchingAtCorner = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.288635 47.618286)"));

        final Rectangle overlapping = Rectangle.forCorners(
                Location.forWkt("POINT(-122.289001 47.6182798)"),
                Location.forWkt("POINT(-122.2886447 47.618416)"));

        Assert.assertEquals("There should be zero intersecting areas", 0,
                Iterables.size(atlas.areasIntersecting(notTouching)));

        Assert.assertEquals("There should be a single intersecting area", 1,
                Iterables.size(atlas.areasIntersecting(fullyInside)));

        Assert.assertEquals("There should be a single intersecting area", 1,
                Iterables.size(atlas.areasIntersecting(touchingAtCorner)));

        Assert.assertEquals("There should be a single intersecting area", 1,
                Iterables.size(atlas.areasIntersecting(overlapping)));
    }

    @Test
    public void testAreasNoIntersectingPolygon()
    {
        final Atlas atlas = this.rule.getNoIntersectionAtlas();

        final Polygon triangle = new Polygon(Location.forString("47.6263, -122.209198"),
                Location.forString("47.628685, -122.209305"),
                Location.forString("47.628704, -122.211761"));

        Assert.assertEquals("There should be no intersecting area", 0,
                Iterables.size(atlas.areasIntersecting(triangle)));

        Assert.assertEquals("There should be no intersecting area", 0,
                Iterables.size(atlas.areasIntersecting(triangle.bounds())));
    }

    @Test
    public void testAreasWithinPolygon()
    {
        final Atlas atlas = this.rule.getWithinTestAtlas();
        final Polygon polygon = Polygon.wkt(WITHIN_TEST_POLYGON_WKT);
        Assert.assertEquals("There are 2 areas within this Polygon", 2,
                Iterables.size(atlas.areasWithin(polygon)));
    }

    @Test
    public void testAtlasItemsWithinPolygon()
    {
        final Atlas atlas = this.rule.getWithinTestAtlas();

        // These are equivalent representations of the same shape.
        final Polygon polygonBoundary = Polygon.wkt(WITHIN_TEST_POLYGON_WKT);
        final Rectangle rectangleBoundary = Rectangle.forCorners(
                Location.forWkt("POINT(-122.289001 47.6182798)"),
                Location.forWkt("POINT(-122.2886447 47.618416)"));

        // However, when the Polygon is not represented as a Rectangle, we rely on the underlying
        // awt definition of insideness - which considers Node 4 from edge 0 to be "outside" the
        // polygon, even though it is on the boundary.
        Assert.assertEquals("There are 2 lines, 2 edges, 2 Areas and 2 Nodes within this Polygon",
                8, Iterables.size(atlas.itemsWithin(polygonBoundary)));

        // If we represent the Polygon as a Rectangle, then we forego the awt call and the same Node
        // 4 is now considered inside. This is an unfortunate side-affect of the awt dependency.
        Assert.assertEquals("There are 2 lines, 2 edges, 2 Areas and 3 Nodes within this Polygon",
                9, Iterables.size(atlas.itemsWithin(rectangleBoundary)));
    }

    @Test
    public void testEdgesIntersectingPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT(-122.288798 47.618472)"),
                Location.forWkt("POINT (-122.288788 47.618482)"));

        final Rectangle touchingAtCorner = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.288635 47.618286)"));

        final Rectangle overlapping = Rectangle.forCorners(
                Location.forWkt("POINT(-122.289001 47.6182798)"),
                Location.forWkt("POINT(-122.2886447 47.618416)"));

        Assert.assertEquals("There should be zero intersections", 0,
                Iterables.size(atlas.edgesIntersecting(notTouching)));

        Assert.assertEquals("There should be a single intersection", 1,
                Iterables.size(atlas.edgesIntersecting(touchingAtCorner)));

        Assert.assertEquals("There should be a single intersection", 1,
                Iterables.size(atlas.edgesIntersecting(overlapping)));
    }

    @Test
    public void testEdgesWithinPolygon()
    {
        final Atlas atlas = this.rule.getWithinTestAtlas();
        final Polygon polygon = Polygon.wkt(WITHIN_TEST_POLYGON_WKT);
        Assert.assertEquals("There are exactly 2 edges within this Polygon", 2,
                Iterables.size(atlas.edgesWithin(polygon)));
    }

    @Test
    public void testLineItemsWithinPolygon()
    {
        final Atlas atlas = this.rule.getWithinTestAtlas();
        final Polygon polygon = Polygon.wkt(WITHIN_TEST_POLYGON_WKT);
        Assert.assertEquals("There are 2 lines and 2 edges within this Polygon", 4,
                Iterables.size(atlas.lineItemsWithin(polygon)));
    }

    @Test
    public void testLinesIntersectingPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT(-122.288798 47.618472)"),
                Location.forWkt("POINT (-122.288788 47.618482)"));

        final Rectangle touchingAtCorner = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.288635 47.618286)"));

        final Rectangle overlapping = Rectangle.forCorners(
                Location.forWkt("POINT (-122.289001 47.6182798)"),
                Location.forWkt("POINT(-122.2886447 47.618416)"));

        Assert.assertEquals("There should be zero intersections", 0,
                Iterables.size(atlas.linesIntersecting(notTouching)));

        Assert.assertEquals("There should be a single intersection", 1,
                Iterables.size(atlas.linesIntersecting(touchingAtCorner)));

        Assert.assertEquals("There should be a single intersection", 1,
                Iterables.size(atlas.linesIntersecting(overlapping)));
    }

    @Test
    public void testLinesWithinPolygon()
    {
        final Atlas atlas = this.rule.getWithinTestAtlas();
        final Polygon polygon = Polygon.wkt(WITHIN_TEST_POLYGON_WKT);
        Assert.assertEquals("There are exactly 2 lines within this Polygon", 2,
                Iterables.size(atlas.linesWithin(polygon)));
    }

    @Test
    public void testNodesWithinPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT(-122.288798 47.618472)"),
                Location.forWkt("POINT(-122.288788 47.618482)"));

        final Rectangle touchingAtCorner = Rectangle.forCorners(
                Location.forWkt("POINT(-122.289001 47.6182798)"),
                Location.forWkt("POINT(-122.2886447 47.618416)"));

        final Rectangle containingMultipleNodes = Rectangle.forCorners(
                Location.forWkt("POINT(-122.289001 47.6182798)"),
                Location.forWkt("POINT(-122.2886447 47.621016)"));

        Assert.assertEquals("There should be zero nodes in this Polygon", 0,
                Iterables.size(atlas.nodesWithin(notTouching)));

        Assert.assertEquals("There should be a single node in this Polygon", 1,
                Iterables.size(atlas.nodesWithin(touchingAtCorner)));

        Assert.assertEquals("There should be two nodes in this Polygon", 2,
                Iterables.size(atlas.nodesWithin(containingMultipleNodes)));
    }

    @Test
    public void testRelationsIntersectingPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT(-122.288798 47.618472)"),
                Location.forWkt("POINT(-122.288788 47.618482)"));

        final Rectangle intersectingSingleEdge = Rectangle.forCorners(
                Location.forWkt("POINT(-122.289001 47.6182798)"),
                Location.forWkt("POINT(-122.2886447 47.618416)"));

        final Rectangle containingMultipleEdges = Rectangle.forCorners(
                Location.forWkt("POINT(-122.289001 47.6182798)"),
                Location.forWkt("POINT(-122.2886447 47.621016)"));

        Assert.assertEquals("There should be no parts of the relation touching this Polygon", 0,
                Iterables.size(atlas.relationsWithEntitiesIntersecting(notTouching)));

        Assert.assertEquals(
                "There should be a single edge, part of one relation, running through this Polygon",
                1, Iterables.size(atlas.relationsWithEntitiesIntersecting(intersectingSingleEdge)));

        Assert.assertEquals(
                "There should be two edges, both part of the same relation, running through this Polygon",
                1,
                Iterables.size(atlas.relationsWithEntitiesIntersecting(containingMultipleEdges)));
    }

    @Test
    public void testRelationsWithinPolygon()
    {
        final Atlas atlas = this.rule.getWithinTestAtlas();
        final Polygon polygon = Polygon.wkt(WITHIN_TEST_POLYGON_WKT);
        Assert.assertEquals(
                "There is a single relation that has all members fully within the polygon", 1,
                Iterables.size(atlas.relationsWithEntitiesWithin(polygon)));
    }

}

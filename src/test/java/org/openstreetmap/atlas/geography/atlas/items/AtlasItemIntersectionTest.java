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
    @Rule
    public final AtlasItemIntersectionTestRule rule = new AtlasItemIntersectionTestRule();

    @Test
    public void testAreasIntersectingPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT (-122.288925 47.618916)"),
                Location.forWkt("POINT (-122.288935 47.618946)"));

        final Rectangle fullyInside = Rectangle.forCorners(
                Location.forWkt("POINT (-122.288798 47.618482)"),
                Location.forWkt("POINT (-122.288788 47.618472)"));

        final Rectangle touchingAtCorner = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.288635 47.618286)"));

        final Rectangle overlapping = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.289001 47.618416)"));

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
    public void testEdgesIntersectingPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT (-122.288798 47.618482)"),
                Location.forWkt("POINT (-122.288788 47.618472)"));

        final Rectangle touchingAtCorner = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.288635 47.618286)"));

        final Rectangle overlapping = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.289001 47.618416)"));

        Assert.assertEquals("There should be zero intersections", 0,
                Iterables.size(atlas.edgesIntersecting(notTouching)));

        Assert.assertEquals("There should be a single intersection", 1,
                Iterables.size(atlas.edgesIntersecting(touchingAtCorner)));

        Assert.assertEquals("There should be a single intersection", 1,
                Iterables.size(atlas.edgesIntersecting(overlapping)));
    }

    @Test
    public void testLinesIntersectingPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT (-122.288798 47.618482)"),
                Location.forWkt("POINT (-122.288788 47.618472)"));

        final Rectangle touchingAtCorner = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.288635 47.618286)"));

        final Rectangle overlapping = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.289001 47.618416)"));

        Assert.assertEquals("There should be zero intersections", 0,
                Iterables.size(atlas.linesIntersecting(notTouching)));

        Assert.assertEquals("There should be a single intersection", 1,
                Iterables.size(atlas.linesIntersecting(touchingAtCorner)));

        Assert.assertEquals("There should be a single intersection", 1,
                Iterables.size(atlas.linesIntersecting(overlapping)));
    }

    @Test
    public void testNodesWithinPolygon()
    {
        final Atlas atlas = this.rule.getIntersectionAtlas();

        final Rectangle notTouching = Rectangle.forCorners(
                Location.forWkt("POINT (-122.288798 47.618482)"),
                Location.forWkt("POINT (-122.288788 47.618472)"));

        final Rectangle touchingAtCorner = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.289001 47.618416)"));

        final Rectangle containingMultipleNodes = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.289001 47.621016)"));

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
                Location.forWkt("POINT (-122.288798 47.618482)"),
                Location.forWkt("POINT (-122.288788 47.618472)"));

        final Rectangle intersectingSingleEdge = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.289001 47.618416)"));

        final Rectangle containingMultipleEdges = Rectangle.forCorners(
                Location.forWkt("POINT (-122.2886447 47.6182798)"),
                Location.forWkt("POINT (-122.289001 47.621016)"));

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

}

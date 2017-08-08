package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * Tests overlap/containment/intersection functionality between a {@link Polygon} and
 * {@link PolyLine}.
 *
 * @author mgostintsev
 */
public class PolyLineCoveringPolygonTest
{
    @Rule
    public final PolyLineCoveringPolygonTestRule rule = new PolyLineCoveringPolygonTestRule();

    @Test
    public void testPolyLinesAsEntirePolygonBoundary()
    {
        final Atlas atlas = this.rule.getPolyLinesAsEntirePolygonBoundaryAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testPolyLinesAsPartOfPolygonBoundary()
    {
        final Atlas atlas = this.rule.getPolyLinesAsPartOfPolygonBoundaryAtlas();
        verifyOverlapExists(atlas);
        verifyFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testPolyLinesCuttingThroughPolygon()
    {
        final Atlas atlas = this.rule.getPolyLinesCuttingThroughPolygonAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testPolyLinesFullyInsidePolygon()
    {
        final Atlas atlas = this.rule.getPolyLinesFullyInsidePolygonAtlas();
        verifyOverlapExists(atlas);
        verifyFullContainment(atlas);
        verifyNoIntersection(atlas);
    }

    @Test
    public void testPolyLinesFullyOutsidePolygon()
    {
        final Atlas atlas = this.rule.getPolyLinesFullyOutsidePolygonAtlas();
        verifyNoOverlap(atlas);
        verifyNoFullContainment(atlas);
        verifyNoIntersection(atlas);
    }

    @Test
    public void testPolyLinesHalfwayInsidePolygon()
    {
        final Atlas atlas = this.rule.getPolyLinesHalfwayInsidePolygonAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testPolyLinesTouchingPolygonBoundary()
    {
        final Atlas atlas = this.rule.getPolyLinesTouchingPolygonBoundaryAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testPolyLinesTouchingPolygonVertex()
    {
        final Atlas atlas = this.rule.getPolyLinesTouchingPolygonVertexAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    private void verifyFullContainment(final Atlas atlas)
    {
        Assert.assertTrue(atlas.area(1).asPolygon()
                .fullyGeometricallyEncloses(atlas.edge(159019301).asPolyLine()));
        Assert.assertTrue(atlas.area(1).asPolygon()
                .fullyGeometricallyEncloses(atlas.edge(-159019301).asPolyLine()));
    }

    private void verifyIntersection(final Atlas atlas)
    {
        Assert.assertTrue(atlas.area(1).asPolygon().intersects(atlas.edge(159019301).asPolyLine()));
        Assert.assertTrue(
                atlas.area(1).asPolygon().intersects(atlas.edge(-159019301).asPolyLine()));
    }

    private void verifyNoFullContainment(final Atlas atlas)
    {
        Assert.assertFalse(atlas.area(1).asPolygon()
                .fullyGeometricallyEncloses(atlas.edge(159019301).asPolyLine()));
        Assert.assertFalse(atlas.area(1).asPolygon()
                .fullyGeometricallyEncloses(atlas.edge(-159019301).asPolyLine()));
    }

    private void verifyNoIntersection(final Atlas atlas)
    {
        Assert.assertFalse(
                atlas.area(1).asPolygon().intersects(atlas.edge(159019301).asPolyLine()));
        Assert.assertFalse(
                atlas.area(1).asPolygon().intersects(atlas.edge(-159019301).asPolyLine()));
    }

    private void verifyNoOverlap(final Atlas atlas)
    {
        Assert.assertFalse(atlas.area(1).asPolygon().overlaps(atlas.edge(159019301).asPolyLine()));
        Assert.assertFalse(atlas.area(1).asPolygon().overlaps(atlas.edge(-159019301).asPolyLine()));
    }

    private void verifyOverlapExists(final Atlas atlas)
    {
        Assert.assertTrue(atlas.area(1).asPolygon().overlaps(atlas.edge(159019301).asPolyLine()));
        Assert.assertTrue(atlas.area(1).asPolygon().overlaps(atlas.edge(-159019301).asPolyLine()));
    }

}

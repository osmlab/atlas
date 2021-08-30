package org.openstreetmap.atlas.geography;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * Tests overlap/containment/intersection functionality between multiple {@link Polygon}s.
 *
 * @author mgostintsev
 */
public class PolygonCoveringPolygonTest
{
    @Rule
    public final PolygonCoveringPolygonTestRule rule = new PolygonCoveringPolygonTestRule();

    @Test
    public void testPolygonFullyInsidePolygon()
    {
        final Atlas atlas = this.rule.getPolygonWithinPolygonAtlas();
        verifyOverlapExists(atlas);
        verifyFullContainment(atlas);

        // This may seen un-intuitive, but because the underlying polylines never intersect, this
        // will not have an intersection, even though one Polygon fully encloses the other.
        verifyNoIntersection(atlas);
    }

    @Test
    public void testTwoIdenticalPolygonsStackedOnEachOther()
    {
        final Atlas atlas = this.rule.getPolygonsStackedOnEachOtherAtlas();
        verifyOverlapExists(atlas);
        verifyFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testTwoPolygonsFarApart()
    {
        final Atlas atlas = this.rule.getNonOverlappingNonTouchingPolygonsAtlas();
        verifyNoOverlap(atlas);
        verifyNoFullContainment(atlas);
        verifyNoIntersection(atlas);
    }

    @Test
    public void testTwoPolygonsOverlappingAtCorner()
    {
        final Atlas atlas = this.rule.getPolygonsOverlappingAtCornerAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testTwoPolygonsOverlappingAtSide()
    {
        final Atlas atlas = this.rule.getPolygonsOverlappingAtSideAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testTwoPolygonsSharingSide()
    {
        final Atlas atlas = this.rule.getPolygonsSharingSideAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    @Test
    public void testTwoPolygonsTouchingAtVertex()
    {
        final Atlas atlas = this.rule.getPolygonsTouchingAtVertexAtlas();
        verifyOverlapExists(atlas);
        verifyNoFullContainment(atlas);
        verifyIntersection(atlas);
    }

    private void verifyFullContainment(final Atlas atlas)
    {
        Assert.assertTrue(
                atlas.area(1).asPolygon().fullyGeometricallyEncloses(atlas.area(2).asPolygon()));
    }

    private void verifyIntersection(final Atlas atlas)
    {
        Assert.assertTrue(atlas.area(1).asPolygon().intersects(atlas.area(2).asPolygon()));
    }

    private void verifyNoFullContainment(final Atlas atlas)
    {
        Assert.assertFalse(
                atlas.area(1).asPolygon().fullyGeometricallyEncloses(atlas.area(2).asPolygon()));
    }

    private void verifyNoIntersection(final Atlas atlas)
    {
        Assert.assertFalse(atlas.area(1).asPolygon().intersects(atlas.area(2).asPolygon()));
    }

    private void verifyNoOverlap(final Atlas atlas)
    {
        Assert.assertFalse(atlas.area(1).asPolygon().overlaps(atlas.area(2).asPolygon()));
    }

    private void verifyOverlapExists(final Atlas atlas)
    {
        Assert.assertTrue(atlas.area(1).asPolygon().overlaps(atlas.area(2).asPolygon()));
    }
}

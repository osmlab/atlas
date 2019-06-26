package org.openstreetmap.atlas.geography.atlas.items.complex.roundabout;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Unit tests for {@link ComplexRoundabout}.
 *
 * @author bbreithaupt
 */
public class ComplexRoundaboutTest
{
    @Rule
    public ComplexRoundaboutTestRule setup = new ComplexRoundaboutTestRule();

    @Test
    public void clockwiseRoundaboutLeftDrivingTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.clockwiseRoundaboutLeftDrivingAtlas().edge(1234));
        Assert.assertTrue(complexRoundabout.isValid());
        Assert.assertEquals(5, complexRoundabout.getRoundaboutEdgeSet().size());
    }

    @Test
    public void clockwiseRoundaboutRightDrivingTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.clockwiseRoundaboutRightDrivingAtlas().edge(1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertEquals(ComplexRoundabout.WRONG_WAY_INVALIDATION,
                complexRoundabout.getAllInvalidations().get(0).getReason());
    }

    @Test
    public void clockwiseRoundaboutRightDrivingMadeLeftDrivingTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.clockwiseRoundaboutRightDrivingAtlas().edge(1234),
                Collections.singletonList("USA"));
        Assert.assertTrue(complexRoundabout.isValid());
        Assert.assertEquals(5, complexRoundabout.getRoundaboutEdgeSet().size());
    }

    @Test
    public void counterClockwiseRoundaboutLeftDrivingTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.counterClockwiseRoundaboutLeftDrivingAtlas().edge(1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertEquals(ComplexRoundabout.WRONG_WAY_INVALIDATION,
                complexRoundabout.getAllInvalidations().get(0).getReason());
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.counterClockwiseRoundaboutRightDrivingAtlas().edge(1244));
        Assert.assertTrue(complexRoundabout.isValid());
        Assert.assertEquals(5, complexRoundabout.getRoundaboutEdgeSet().size());
    }

    @Test
    public void multiDirectionalRoundaboutTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.multiDirectionalRoundaboutAtlas().edge(1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertEquals(ComplexRoundabout.INCOMPLETE_ROUTE_INVALIDATION,
                complexRoundabout.getAllInvalidations().get(0).getReason());
    }

    @Test
    public void clockwiseRoundaboutLeftDrivingMissingTagTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.clockwiseRoundaboutLeftDrivingMissingTagAtlas().edge(1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertEquals(ComplexRoundabout.INCOMPLETE_ROUTE_INVALIDATION,
                complexRoundabout.getAllInvalidations().get(0).getReason());
    }

    @Test
    public void counterClockwiseConnectedDoubleRoundaboutRightDrivingTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas().edge(1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertEquals(ComplexRoundabout.INCOMPLETE_ROUTE_INVALIDATION,
                complexRoundabout.getAllInvalidations().get(0).getReason());
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingOutsideConnectionTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(this.setup
                .counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas().edge(1234));
        Assert.assertTrue(complexRoundabout.isValid());
        Assert.assertEquals(5, complexRoundabout.getRoundaboutEdgeSet().size());
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingOneWayNoTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.counterClockwiseRoundaboutRightDrivingOneWayNoAtlas().edge(1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertEquals(ComplexRoundabout.INCOMPLETE_ROUTE_INVALIDATION,
                complexRoundabout.getAllInvalidations().get(0).getReason());
    }

    @Test
    public void counterClockwiseRoundaboutRightDrivingNonCarNavigableTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas().edge(1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertEquals(ComplexRoundabout.INCOMPLETE_ROUTE_INVALIDATION,
                complexRoundabout.getAllInvalidations().get(0).getReason());
    }

    @Test
    public void clockwiseRoundaboutRightDrivingIncompleteTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.clockwiseRoundaboutRightDrivingIncompleteAtlas().edge(1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(2, complexRoundabout.getAllInvalidations().size());
    }

    @Test
    public void nonRoundaboutSourceTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.clockwiseRoundaboutLeftDrivingMissingTagAtlas().edge(1238));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertTrue(complexRoundabout.getAllInvalidations().get(0).getReason()
                .contains("Invalid source Edge"));
    }

    @Test
    public void nonMasterSourceTest()
    {
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout(
                this.setup.counterClockwiseRoundaboutRightDrivingOneWayNoAtlas().edge(-1234));
        Assert.assertFalse(complexRoundabout.isValid());
        Assert.assertEquals(1, complexRoundabout.getAllInvalidations().size());
        Assert.assertTrue(complexRoundabout.getAllInvalidations().get(0).getReason()
                .contains("Invalid source Edge"));
    }

    @Test
    public void validValidFinderTest()
    {
        Assert.assertEquals(2,
                Iterables.size(new ComplexRoundaboutFinder()
                        .find(new MultiAtlas(this.setup.clockwiseRoundaboutLeftDrivingAtlas(),
                                this.setup.counterClockwiseRoundaboutRightDrivingAtlas()))));
    }

    @Test
    public void invalidValidFinderTest()
    {
        Assert.assertEquals(1,
                Iterables.size(new ComplexRoundaboutFinder()
                        .find(new MultiAtlas(this.setup.clockwiseRoundaboutRightDrivingAtlas(),
                                this.setup.counterClockwiseRoundaboutRightDrivingAtlas()))));
    }
}

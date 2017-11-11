package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for {@link org.openstreetmap.atlas.tags.TurnLanesTag}
 *
 * @author brian_l_davis
 */
public class TurnLaneTagTestCase
{
    @Test
    public void testBackwardTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesBackwardTag.KEY,
                "left|through|right");
        Assert.assertTrue(TurnLanesBackwardTag.getBackwardTurnLanes(taggable).isPresent());
        Assert.assertTrue(
                TurnLanesBackwardTag.hasBackwardTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesTag.hasTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
    }

    @Test
    public void testForwardTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesForwardTag.KEY,
                "left|through|right");
        Assert.assertTrue(TurnLanesForwardTag.getForwardTurnLanes(taggable).isPresent());
        Assert.assertTrue(
                TurnLanesForwardTag.hasForwardTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesTag.hasTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesTag.hasTurnLane(taggable));
    }

    @Test
    public void testMalformedTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesTag.KEY, "sdf|sldkf;jsdl|sldkfj");
        Assert.assertTrue(TurnLanesTag.hasTurnLane(taggable));
        Assert.assertFalse(TurnLanesTag.hasTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
    }

    @Test
    public void testMatchSpecificTurnLaneTags()
    {
        final TestTaggable forwardTurnLaneTagged = new TestTaggable(TurnLanesForwardTag.KEY,
                "right");
        final TestTaggable backwardTurnLaneTagged = new TestTaggable(TurnLanesBackwardTag.KEY,
                "left");

        Assert.assertTrue(TurnTag.hasTurn(forwardTurnLaneTagged));
        Assert.assertTrue(TurnTag.hasTurn(forwardTurnLaneTagged, TurnTag.TurnType.RIGHT));

        Assert.assertTrue(TurnTag.hasTurn(backwardTurnLaneTagged));
        Assert.assertTrue(TurnTag.hasTurn(backwardTurnLaneTagged, TurnTag.TurnType.LEFT));
    }

    @Test
    public void testMultipleTypeTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesTag.KEY, "left;through|right");
        Assert.assertTrue(TurnLanesTag.hasTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesTag.hasTurnLane(taggable));
    }

    @Test
    public void testSingleTypeTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesTag.KEY, "left|through|right");
        Assert.assertTrue(TurnLanesTag.hasTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesTag.hasTurnLane(taggable));
    }

    @Test
    public void testUntagged()
    {
        final TestTaggable taggable = new TestTaggable(HighwayTag.KEY, "no");
        Assert.assertFalse(TurnLanesTag.getTurnLanes(taggable).isPresent());
        Assert.assertFalse(TurnLanesTag.hasTurnLane(taggable));
    }
}

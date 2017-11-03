package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for {@link org.openstreetmap.atlas.tags.TurnLanesForwardTag}
 *
 * @author brian_l_davis
 */
public class TurnLaneForwardTagTestCase
{
    @Test
    public void testForwardTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesForwardTag.KEY,
                "left|through|right");
        Assert.assertTrue(TurnLanesForwardTag.getForwardTurnLanes(taggable).isPresent());
        Assert.assertTrue(
                TurnLanesForwardTag.hasForwardTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
    }

    @Test
    public void testMalformedTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesForwardTag.KEY,
                "sdf|sldkf;jsdl|sldkfj");
        Assert.assertTrue(TurnLanesForwardTag.hasForwardTurnLane(taggable));
        Assert.assertFalse(TurnLanesForwardTag.hasForwardTurnLane(taggable,
                TurnLanesForwardTag.TurnType.THROUGH));
    }

    @Test
    public void testMultipleTypeTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesForwardTag.KEY,
                "left;through|right");
        Assert.assertTrue(TurnLanesForwardTag.hasForwardTurnLane(taggable,
                TurnLanesForwardTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesForwardTag.hasForwardTurnLane(taggable));
    }

    @Test
    public void testSingleTypeTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesForwardTag.KEY,
                "left|through|right");
        Assert.assertTrue(TurnLanesForwardTag.hasForwardTurnLane(taggable,
                TurnLanesForwardTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesForwardTag.hasForwardTurnLane(taggable));
    }

    @Test
    public void testUntagged()
    {
        final TestTaggable taggable = new TestTaggable(HighwayTag.KEY, "no");
        Assert.assertFalse(TurnLanesForwardTag.getForwardTurnLanes(taggable).isPresent());
        Assert.assertFalse(TurnLanesForwardTag.hasForwardTurnLane(taggable));
    }
}

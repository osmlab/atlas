package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for {@link org.openstreetmap.atlas.tags.TurnLanesBackwardTag}
 *
 * @author brian_l_davis
 */
public class TurnLaneBackwardTagTestCase
{
    @Test
    public void testBackwardTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesBackwardTag.KEY,
                "left|through|right");
        Assert.assertTrue(TurnLanesBackwardTag.getBackwardTurnLanes(taggable).isPresent());
        Assert.assertTrue(
                TurnLanesBackwardTag.hasBackwardTurnLane(taggable, TurnLanesTag.TurnType.THROUGH));
    }

    @Test
    public void testMalformedTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesBackwardTag.KEY,
                "sdf|sldkf;jsdl|sldkfj");
        Assert.assertTrue(TurnLanesBackwardTag.hasBackwardTurnLane(taggable));
        Assert.assertFalse(TurnLanesBackwardTag.hasBackwardTurnLane(taggable,
                TurnLanesBackwardTag.TurnType.THROUGH));
    }

    @Test
    public void testMultipleTypeTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesBackwardTag.KEY,
                "left;through|right");
        Assert.assertTrue(TurnLanesBackwardTag.hasBackwardTurnLane(taggable,
                TurnLanesBackwardTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesBackwardTag.hasBackwardTurnLane(taggable));
    }

    @Test
    public void testSingleTypeTurnLane()
    {
        final TestTaggable taggable = new TestTaggable(TurnLanesBackwardTag.KEY,
                "left|through|right");
        Assert.assertTrue(TurnLanesBackwardTag.hasBackwardTurnLane(taggable,
                TurnLanesBackwardTag.TurnType.THROUGH));
        Assert.assertTrue(TurnLanesBackwardTag.hasBackwardTurnLane(taggable));
    }

    @Test
    public void testUntagged()
    {
        final TestTaggable taggable = new TestTaggable(HighwayTag.KEY, "no");
        Assert.assertFalse(TurnLanesBackwardTag.getBackwardTurnLanes(taggable).isPresent());
        Assert.assertFalse(TurnLanesBackwardTag.hasBackwardTurnLane(taggable));
    }
}

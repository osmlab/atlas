package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for {@link org.openstreetmap.atlas.tags.TurnTag}
 *
 * @author brian_l_davis
 */
public class TurnTagTestCase
{
    @Test
    public void testMalformedTurn()
    {
        final TestTaggable taggable = new TestTaggable(TurnTag.KEY, "sdf|sldkf;jsdl|sldkfj");
        Assert.assertTrue(TurnTag.hasTurn(taggable));
        Assert.assertFalse(TurnTag.hasTurn(taggable, TurnTag.TurnType.THROUGH));
    }

    @Test
    public void testMatchSpecificTurnTags()
    {
        final TestTaggable forwardTurnLaneTagged = new TestTaggable(TurnLanesForwardTag.KEY,
                "right");
        final TestTaggable backwardTurnLaneTagged = new TestTaggable(TurnLanesBackwardTag.KEY,
                "left");
        final TestTaggable turnLanedTagged = new TestTaggable(TurnLanesBackwardTag.KEY, "through");

        Assert.assertTrue(TurnTag.hasTurn(forwardTurnLaneTagged));
        Assert.assertTrue(TurnTag.hasTurn(forwardTurnLaneTagged, TurnTag.TurnType.RIGHT));

        Assert.assertTrue(TurnTag.hasTurn(backwardTurnLaneTagged));
        Assert.assertTrue(TurnTag.hasTurn(backwardTurnLaneTagged, TurnTag.TurnType.LEFT));

        Assert.assertTrue(TurnTag.hasTurn(turnLanedTagged));
        Assert.assertTrue(TurnTag.hasTurn(turnLanedTagged, TurnTag.TurnType.THROUGH));
    }

    @Test
    public void testMultipleTypeTurn()
    {
        final TestTaggable taggable = new TestTaggable(TurnTag.KEY, "left;through|right");
        Assert.assertTrue(TurnTag.hasTurn(taggable, TurnTag.TurnType.THROUGH));
        Assert.assertTrue(TurnTag.hasTurn(taggable));
    }

    @Test
    public void testSingleTypeTurn()
    {
        final TestTaggable taggable = new TestTaggable(TurnTag.KEY, "left|through|right");
        Assert.assertTrue(TurnTag.hasTurn(taggable, TurnTag.TurnType.THROUGH));
        Assert.assertTrue(TurnTag.hasTurn(taggable));
    }

    @Test
    public void testUntagged()
    {
        final TestTaggable taggable = new TestTaggable(HighwayTag.KEY, "no");
        Assert.assertFalse(TurnTag.getTurns(taggable).isPresent());
        Assert.assertFalse(TurnTag.hasTurn(taggable));
    }
}

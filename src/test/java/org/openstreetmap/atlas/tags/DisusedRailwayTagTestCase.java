package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author vlemberg
 */

public class DisusedRailwayTagTestCase
{
    private final Taggable taggable1 = Taggable.with("disused:railway", "level_crossing");
    private final Taggable taggable2 = Taggable.with("disused:railway", "crossing");
    private final Taggable taggable3 = Taggable.with("railway", "level_crossing");

    @Test
    public void isDisuseRailwayCrossing()
    {
        Assert.assertTrue(DisusedRailwayTag.isDisusedRailwayCrossing(this.taggable1));
        Assert.assertTrue(DisusedRailwayTag.isDisusedRailwayCrossing(this.taggable2));
        Assert.assertFalse(DisusedRailwayTag.isDisusedRailwayCrossing(this.taggable3));
    }
}

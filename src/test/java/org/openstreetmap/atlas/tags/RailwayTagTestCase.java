package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author vlemberg
 */

public class RailwayTagTestCase
{
    private final Taggable taggable1 = Taggable.with("railway", "level_crossing");
    private final Taggable taggable2 = Taggable.with("railway", "tram_level_crossing");
    private final Taggable taggable3 = Taggable.with("disused:railway", "level_crossing");

    @Test
    public void isRailwayCrossing()
    {
        Assert.assertTrue(RailwayTag.isRailwayCrossing(this.taggable1));
        Assert.assertTrue(RailwayTag.isRailwayCrossing(this.taggable2));
        Assert.assertFalse(RailwayTag.isRailwayCrossing(this.taggable3));
    }
}

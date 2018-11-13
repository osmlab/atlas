package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;

public class ChangeAtlasTest
{
    @Rule
    public ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    @Test
    public void testEdge()
    {
        final Atlas atlas = this.rule.getAtlasEdge();
        System.out.println(atlas);
    }
}

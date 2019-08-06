package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * @author matthieun
 */
public class LineItemTest
{
    @Rule
    public final LineItemTestRule rule = new LineItemTestRule();

    @Test
    public void testOverallHeading()
    {
        final Atlas atlas = this.rule.getoverallHeadingAtlas();
        final Line linear = atlas.line(1L);
        final Line loop = atlas.line(2L);
        Assert.assertTrue(linear.overallHeading().isPresent());
        Assert.assertEquals(725290933L, linear.overallHeading().get().asDm7());
        Assert.assertFalse(loop.overallHeading().isPresent());
    }
}

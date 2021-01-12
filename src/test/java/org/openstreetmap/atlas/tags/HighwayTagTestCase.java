package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for {@link org.openstreetmap.atlas.tags.HighwayTag}
 *
 * @author v-garei
 */
public class HighwayTagTestCase
{

    @Test
    public void emergencyBayIsHighwayTestCase()
    {
        final Taggable taggable = Taggable.with("highway", "emergency_bay");
        Assert.assertTrue(Validators.hasValuesFor(taggable, HighwayTag.class));
    }

    @Test
    public void testFalseNodeOnlyHighwayTag()
    {
        final TestTaggable falseNodeOnlyTag = new TestTaggable(HighwayTag.KEY, "trunk");

        Assert.assertFalse(HighwayTag.isNodeOnlyTag(falseNodeOnlyTag));
    }

    @Test
    public void testFalseWayOnlyHighwayTag()
    {
        final TestTaggable falseWayOnlyTag = new TestTaggable(HighwayTag.KEY, "bus_stop");

        Assert.assertFalse(HighwayTag.isWayOnlyTag(falseWayOnlyTag));
    }

    @Test
    public void testTrueNodeOnlyHighwayTag()
    {
        final TestTaggable trueNodeOnlyTag = new TestTaggable(HighwayTag.KEY, "emergency_bay");

        Assert.assertTrue(HighwayTag.isNodeOnlyTag(trueNodeOnlyTag));
    }

    @Test
    public void testTrueWayOnlyHighwayTag()
    {
        final TestTaggable trueWayOnlyTag = new TestTaggable(HighwayTag.KEY, "primary");

        Assert.assertTrue(HighwayTag.isWayOnlyTag(trueWayOnlyTag));
    }

    @Test
    public void trafficMirrorIsHighwayTestCase()
    {
        final Taggable taggable = Taggable.with("highway", "traffic_mirror");
        Assert.assertTrue(Validators.hasValuesFor(taggable, HighwayTag.class));
    }

    @Test
    public void trailheadIsHighwayTestCase()
    {
        final Taggable taggable = Taggable.with("highway", "trailhead");
        Assert.assertTrue(Validators.hasValuesFor(taggable, HighwayTag.class));
    }
}

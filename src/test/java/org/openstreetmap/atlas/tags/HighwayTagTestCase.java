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
    public void highwayTagTests()
    {
        final Taggable emergencyBayTaggable = Taggable.with("highway", "emergency_bay");
        Assert.assertTrue(Validators.hasValuesFor(emergencyBayTaggable, HighwayTag.class));

        final TestTaggable falseNodeOnlyTag = new TestTaggable(HighwayTag.KEY, "trunk");
        Assert.assertFalse(HighwayTag.isNodeOnlyTag(falseNodeOnlyTag));

        final TestTaggable falseWayOnlyTag = new TestTaggable(HighwayTag.KEY, "bus_stop");
        Assert.assertFalse(HighwayTag.isWayOnlyTag(falseWayOnlyTag));

        final TestTaggable trueNodeOnlyTag = new TestTaggable(HighwayTag.KEY, "traffic_mirror");
        Assert.assertTrue(HighwayTag.isNodeOnlyTag(trueNodeOnlyTag));

        final TestTaggable trueWayOnlyTag = new TestTaggable(HighwayTag.KEY, "primary");
        Assert.assertTrue(HighwayTag.isWayOnlyTag(trueWayOnlyTag));

        final Taggable trafficMirrorTaggable = Taggable.with("highway", "traffic_mirror");
        Assert.assertTrue(Validators.hasValuesFor(trafficMirrorTaggable, HighwayTag.class));

        final Taggable trailheadTaggable = Taggable.with("highway", "trailhead");
        Assert.assertTrue(Validators.hasValuesFor(trailheadTaggable, HighwayTag.class));
    }
}

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
        final Taggable emergencyBayTaggable = Taggable.with(HighwayTag.KEY,
                HighwayTag.EMERGENCY_BAY.toString());
        Assert.assertTrue(Validators.hasValuesFor(emergencyBayTaggable, HighwayTag.class));

        final TestTaggable falseNodeOnlyTag = new TestTaggable(HighwayTag.KEY,
                HighwayTag.TRUNK.toString());
        Assert.assertFalse(HighwayTag.isNodeOnlyTag(falseNodeOnlyTag));

        final TestTaggable falseWayOnlyTag = new TestTaggable(HighwayTag.KEY,
                HighwayTag.BUS_STOP.toString());
        Assert.assertFalse(HighwayTag.isWayOnlyTag(falseWayOnlyTag));

        final TestTaggable trueNodeOnlyTag = new TestTaggable(HighwayTag.KEY,
                HighwayTag.TRAFFIC_MIRROR.toString());
        Assert.assertTrue(HighwayTag.isNodeOnlyTag(trueNodeOnlyTag));

        final TestTaggable trueWayOnlyTag = new TestTaggable(HighwayTag.KEY,
                HighwayTag.PRIMARY.toString());
        Assert.assertTrue(HighwayTag.isWayOnlyTag(trueWayOnlyTag));

        final Taggable trafficMirrorTaggable = Taggable.with(HighwayTag.KEY,
                HighwayTag.TRAFFIC_MIRROR.toString());
        Assert.assertTrue(Validators.hasValuesFor(trafficMirrorTaggable, HighwayTag.class));

        final Taggable trailheadTaggable = Taggable.with(HighwayTag.KEY,
                HighwayTag.TRAILHEAD.toString());
        Assert.assertTrue(Validators.hasValuesFor(trailheadTaggable, HighwayTag.class));
    }
}

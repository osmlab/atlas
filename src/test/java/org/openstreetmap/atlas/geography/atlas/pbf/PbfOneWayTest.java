package org.openstreetmap.atlas.geography.atlas.pbf;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfOneWay;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.OneWayTag;
import org.openstreetmap.atlas.tags.Taggable;

/**
 * Testing {@link PbfOneWay} functionality.
 *
 * @author mgostintsev
 */
public class PbfOneWayTest
{
    @Test
    public void testOneWayCriteria()
    {
        final Taggable primary = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase());
        final Taggable closedNeighborhood = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.NO.name().toLowerCase());
        final Taggable motorway = Taggable.with(HighwayTag.KEY,
                HighwayTag.MOTORWAY.name().toLowerCase());
        final Taggable twoWayMotorway = Taggable.with(HighwayTag.KEY,
                HighwayTag.MOTORWAY.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.NO.name().toLowerCase());
        final Taggable motorwayLink = Taggable.with(HighwayTag.KEY,
                HighwayTag.MOTORWAY_LINK.name().toLowerCase());
        final Taggable roundabout = Taggable.with(JunctionTag.KEY,
                JunctionTag.ROUNDABOUT.name().toLowerCase());
        final Taggable explicitlyTwoWay = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.NO.name().toLowerCase());
        final Taggable explicitlyTwoWayUsingFalse = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.FALSE.name().toLowerCase());
        final Taggable explicitlyTwoWayUsingZero = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY, "0");
        final Taggable explicitlyOneWay = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.YES.name().toLowerCase());
        final Taggable explicitlyOneWayUsingTrue = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.TRUE.name().toLowerCase());
        final Taggable explicitlyOneWayUsingOne = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY, "1");
        final Taggable oneWayReversible = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.REVERSIBLE.name().toLowerCase());
        final Taggable oneWayReversed = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.REVERSE.name().toLowerCase());
        final Taggable oneWayReversedUsingNegativeOne = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY, "-1");

        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(primary));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(motorwayLink));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(twoWayMotorway));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(explicitlyTwoWay));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(explicitlyTwoWayUsingZero));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(explicitlyTwoWayUsingFalse));

        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(motorway));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(roundabout));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(explicitlyOneWay));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(explicitlyOneWayUsingTrue));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(explicitlyOneWayUsingOne));

        Assert.assertEquals(PbfOneWay.CLOSED, PbfOneWay.forTag(closedNeighborhood));
        Assert.assertEquals(PbfOneWay.CLOSED, PbfOneWay.forTag(oneWayReversible));

        Assert.assertEquals(PbfOneWay.REVERSED, PbfOneWay.forTag(oneWayReversed));
        Assert.assertEquals(PbfOneWay.REVERSED, PbfOneWay.forTag(oneWayReversedUsingNegativeOne));
    }
}

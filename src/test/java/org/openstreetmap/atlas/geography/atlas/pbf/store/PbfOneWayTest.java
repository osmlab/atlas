package org.openstreetmap.atlas.geography.atlas.pbf.store;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.MotorVehicleTag;
import org.openstreetmap.atlas.tags.MotorcarTag;
import org.openstreetmap.atlas.tags.OneWayTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.VehicleTag;

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
        final Taggable primaryAccessNoMotorVehicleYes = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.NO.name().toLowerCase(), MotorVehicleTag.KEY,
                MotorVehicleTag.YES.name().toLowerCase());
        final Taggable primaryAccessNoMotorCarYes = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.NO.name().toLowerCase(), MotorcarTag.KEY,
                MotorcarTag.YES.name().toLowerCase());
        final Taggable primaryAccessNoVehicleYes = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.NO.name().toLowerCase(), VehicleTag.KEY,
                VehicleTag.YES.name().toLowerCase());
        final Taggable primaryAccessNoMotorVehicleYesOneWayTrue = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.NO.name().toLowerCase(), MotorVehicleTag.KEY,
                MotorVehicleTag.YES.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.TRUE.name().toLowerCase());
        final Taggable primaryAccessNoMotorCarYesOneWayTrue = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.NO.name().toLowerCase(), MotorcarTag.KEY,
                MotorcarTag.YES.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.TRUE.name().toLowerCase());
        final Taggable primaryAccessNoVehicleYesOneWayTrue = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.NO.name().toLowerCase(), VehicleTag.KEY,
                VehicleTag.YES.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.TRUE.name().toLowerCase());
        final Taggable primaryMotorVehicleNo = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), MotorVehicleTag.KEY,
                MotorVehicleTag.NO.name().toLowerCase());
        final Taggable primaryAccessYesVehicleNo = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.YES.name().toLowerCase(), MotorVehicleTag.KEY,
                MotorVehicleTag.NO.name().toLowerCase());
        final Taggable primaryAccessDeliveryMotorcarNo = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), AccessTag.KEY,
                AccessTag.DELIVERY.name().toLowerCase(), MotorcarTag.KEY,
                MotorcarTag.NO.name().toLowerCase());
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
        final Taggable oneWayReversed = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY,
                OneWayTag.REVERSE.name().toLowerCase());
        final Taggable oneWayReversedUsingNegativeOne = Taggable.with(HighwayTag.KEY,
                HighwayTag.PRIMARY.name().toLowerCase(), OneWayTag.KEY, "-1");
        final Taggable ferryMotorVehicleNo = Taggable.with(RouteTag.KEY,
                RouteTag.FERRY.name().toLowerCase(), MotorVehicleTag.KEY,
                MotorVehicleTag.NO.name().toLowerCase());

        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(primary));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(motorwayLink));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(twoWayMotorway));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(explicitlyTwoWay));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(explicitlyTwoWayUsingZero));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(explicitlyTwoWayUsingFalse));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(primaryAccessNoMotorVehicleYes));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(primaryAccessNoMotorCarYes));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(primaryAccessNoVehicleYes));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(primaryMotorVehicleNo));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(primaryAccessYesVehicleNo));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(primaryAccessDeliveryMotorcarNo));
        Assert.assertEquals(PbfOneWay.NO, PbfOneWay.forTag(ferryMotorVehicleNo));

        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(motorway));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(roundabout));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(explicitlyOneWay));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(explicitlyOneWayUsingTrue));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(explicitlyOneWayUsingOne));
        Assert.assertEquals(PbfOneWay.YES,
                PbfOneWay.forTag(primaryAccessNoMotorVehicleYesOneWayTrue));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(primaryAccessNoMotorCarYesOneWayTrue));
        Assert.assertEquals(PbfOneWay.YES, PbfOneWay.forTag(primaryAccessNoVehicleYesOneWayTrue));

        Assert.assertEquals(PbfOneWay.REVERSED, PbfOneWay.forTag(oneWayReversed));
        Assert.assertEquals(PbfOneWay.REVERSED, PbfOneWay.forTag(oneWayReversedUsingNegativeOne));
    }
}

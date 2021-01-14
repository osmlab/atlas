package org.openstreetmap.atlas.tags.oneway;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.oneway.motor.OneWayMotorVehicleTag;
import org.openstreetmap.atlas.tags.oneway.motor.OneWayMotorcarTag;
import org.openstreetmap.atlas.tags.oneway.motor.OneWayVehicleTag;

/**
 * @author matthieun
 */
public class OneWayTagTest
{
    // Un-tagged
    public static final Taggable UNTAGGED = Taggable.with(HighwayTag.KEY,
            HighwayTag.MOTORWAY.toString());

    // OneWay tags
    public static final Taggable ONE_WAY_YES = Taggable.with(OneWayTag.KEY,
            OneWayTag.YES.toString());
    public static final Taggable ONE_WAY_NO = Taggable.with(OneWayTag.KEY, OneWayTag.NO.toString());
    public static final Taggable ONE_WAY_REVERSIBLE = Taggable.with(OneWayTag.KEY,
            OneWayTag.REVERSIBLE.toString());
    public static final Taggable ONE_WAY_TRUE = Taggable.with(OneWayTag.KEY,
            OneWayTag.TRUE.toString());
    public static final Taggable ONE_WAY_FALSE = Taggable.with(OneWayTag.KEY,
            OneWayTag.FALSE.toString());
    public static final Taggable ONE_WAY_ONE = Taggable.with(OneWayTag.KEY,
            OneWayTag.ONE.toString());
    public static final Taggable ONE_WAY_ZERO = Taggable.with(OneWayTag.KEY,
            OneWayTag.ZERO.toString());
    public static final Taggable ONE_WAY_MINUS_ONE = Taggable.with(OneWayTag.KEY,
            OneWayTag.MINUS_1.toString());
    public static final Taggable ONE_WAY_REVERSE = Taggable.with(OneWayTag.KEY,
            OneWayTag.REVERSE.toString());

    // OneWayMotorVehicle
    public static final Taggable ONE_WAY_MOTOR_VEHICLE_YES = Taggable
            .with(OneWayMotorVehicleTag.KEY, OneWayMotorVehicleTag.YES.toString());
    public static final Taggable ONE_WAY_MOTOR_VEHICLE_NO = Taggable.with(OneWayMotorVehicleTag.KEY,
            OneWayMotorVehicleTag.NO.toString());
    public static final Taggable ONE_WAY_MOTOR_VEHICLE_MINUS_ONE = Taggable
            .with(OneWayMotorVehicleTag.KEY, OneWayMotorVehicleTag.MINUS_1.toString());

    // OneWayVehicle
    public static final Taggable ONE_WAY_VEHICLE_YES = Taggable.with(OneWayVehicleTag.KEY,
            OneWayVehicleTag.YES.toString());
    public static final Taggable ONE_WAY_VEHICLE_NO = Taggable.with(OneWayVehicleTag.KEY,
            OneWayVehicleTag.NO.toString());
    public static final Taggable ONE_WAY_VEHICLE_MINUS_ONE = Taggable.with(OneWayVehicleTag.KEY,
            OneWayVehicleTag.MINUS_1.toString());

    // OneWayMotorcar
    public static final Taggable ONE_WAY_MOTORCAR_YES = Taggable.with(OneWayMotorcarTag.KEY,
            OneWayMotorcarTag.YES.toString());
    public static final Taggable ONE_WAY_MOTORCAR_NO = Taggable.with(OneWayMotorcarTag.KEY,
            OneWayMotorcarTag.NO.toString());
    public static final Taggable ONE_WAY_MOTORCAR_MINUS_ONE = Taggable.with(OneWayMotorcarTag.KEY,
            OneWayMotorcarTag.MINUS_1.toString());

    @Test
    public void testOneWayMotorTagsForward()
    {
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayForward(UNTAGGED));

        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_YES));
        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_TRUE));
        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_ONE));

        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_MOTOR_VEHICLE_YES));
        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_VEHICLE_YES));
        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_MOTORCAR_YES));

        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_MOTOR_VEHICLE_NO));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_MOTOR_VEHICLE_MINUS_ONE));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_VEHICLE_NO));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_VEHICLE_MINUS_ONE));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_MOTORCAR_NO));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayForward(ONE_WAY_MOTORCAR_MINUS_ONE));
    }

    @Test
    public void testOneWayMotorTagsReverse()
    {
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayReversed(UNTAGGED));

        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_MINUS_ONE));
        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_REVERSE));

        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_MOTOR_VEHICLE_MINUS_ONE));
        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_VEHICLE_MINUS_ONE));
        Assert.assertTrue(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_MOTORCAR_MINUS_ONE));

        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_MOTOR_VEHICLE_NO));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_MOTOR_VEHICLE_YES));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_VEHICLE_NO));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_VEHICLE_YES));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_MOTORCAR_NO));
        Assert.assertFalse(OneWayTag.isMotorVehicleOneWayReversed(ONE_WAY_MOTORCAR_YES));
    }

    @Test
    public void testOneWayMotorTagsTwoWay()
    {
        Assert.assertTrue(OneWayTag.isMotorVehicleTwoWay(UNTAGGED));

        Assert.assertTrue(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_MOTOR_VEHICLE_NO));
        Assert.assertTrue(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_VEHICLE_NO));
        Assert.assertTrue(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_MOTORCAR_NO));

        Assert.assertFalse(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_MOTOR_VEHICLE_MINUS_ONE));
        Assert.assertFalse(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_MOTOR_VEHICLE_YES));
        Assert.assertFalse(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_VEHICLE_MINUS_ONE));
        Assert.assertFalse(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_VEHICLE_YES));
        Assert.assertFalse(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_MOTORCAR_MINUS_ONE));
        Assert.assertFalse(OneWayTag.isMotorVehicleTwoWay(ONE_WAY_MOTORCAR_YES));
    }

    @Test
    public void testOneWayRegularForward()
    {
        Assert.assertFalse(OneWayTag.isOneWayForward(UNTAGGED));

        Assert.assertTrue(OneWayTag.isOneWayForward(ONE_WAY_YES));
        Assert.assertTrue(OneWayTag.isOneWayForward(ONE_WAY_TRUE));
        Assert.assertTrue(OneWayTag.isOneWayForward(ONE_WAY_ONE));

        Assert.assertFalse(OneWayTag.isOneWayForward(ONE_WAY_NO));
        Assert.assertFalse(OneWayTag.isOneWayForward(ONE_WAY_REVERSIBLE));
        Assert.assertFalse(OneWayTag.isOneWayForward(ONE_WAY_FALSE));
        Assert.assertFalse(OneWayTag.isOneWayForward(ONE_WAY_ZERO));
        Assert.assertFalse(OneWayTag.isOneWayForward(ONE_WAY_MINUS_ONE));
        Assert.assertFalse(OneWayTag.isOneWayForward(ONE_WAY_REVERSE));

    }

    @Test
    public void testOneWayRegularReverse()
    {
        Assert.assertFalse(OneWayTag.isOneWayReversed(UNTAGGED));

        Assert.assertTrue(OneWayTag.isOneWayReversed(ONE_WAY_MINUS_ONE));
        Assert.assertTrue(OneWayTag.isOneWayReversed(ONE_WAY_REVERSE));

        Assert.assertFalse(OneWayTag.isOneWayReversed(ONE_WAY_YES));
        Assert.assertFalse(OneWayTag.isOneWayReversed(ONE_WAY_TRUE));
        Assert.assertFalse(OneWayTag.isOneWayReversed(ONE_WAY_ONE));
        Assert.assertFalse(OneWayTag.isOneWayReversed(ONE_WAY_NO));
        Assert.assertFalse(OneWayTag.isOneWayReversed(ONE_WAY_REVERSIBLE));
        Assert.assertFalse(OneWayTag.isOneWayReversed(ONE_WAY_FALSE));
        Assert.assertFalse(OneWayTag.isOneWayReversed(ONE_WAY_ZERO));
    }

    @Test
    public void testOneWayRegularTwoWay()
    {
        Assert.assertTrue(OneWayTag.isTwoWay(UNTAGGED));

        Assert.assertTrue(OneWayTag.isTwoWay(ONE_WAY_NO));
        Assert.assertTrue(OneWayTag.isTwoWay(ONE_WAY_FALSE));
        Assert.assertTrue(OneWayTag.isTwoWay(ONE_WAY_ZERO));

        Assert.assertFalse(OneWayTag.isTwoWay(ONE_WAY_YES));
        Assert.assertFalse(OneWayTag.isTwoWay(ONE_WAY_TRUE));
        Assert.assertFalse(OneWayTag.isTwoWay(ONE_WAY_ONE));
        Assert.assertFalse(OneWayTag.isTwoWay(ONE_WAY_REVERSIBLE));
        Assert.assertFalse(OneWayTag.isTwoWay(ONE_WAY_MINUS_ONE));
        Assert.assertFalse(OneWayTag.isTwoWay(ONE_WAY_REVERSE));
    }
}

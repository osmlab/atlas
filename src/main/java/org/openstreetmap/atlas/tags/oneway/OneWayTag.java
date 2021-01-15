package org.openstreetmap.atlas.tags.oneway;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValueAs;
import org.openstreetmap.atlas.tags.annotations.TagValueDeprecated;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.oneway.bicycle.BicycleOneWayTag;
import org.openstreetmap.atlas.tags.oneway.bicycle.CyclewayLeftOneWayTag;
import org.openstreetmap.atlas.tags.oneway.bicycle.CyclewayOneWayTag;
import org.openstreetmap.atlas.tags.oneway.bicycle.CyclewayRightOneWayTag;
import org.openstreetmap.atlas.tags.oneway.bicycle.OneWayBicycleTag;
import org.openstreetmap.atlas.tags.oneway.motor.OneWayMotorVehicleTag;
import org.openstreetmap.atlas.tags.oneway.motor.OneWayMotorcarTag;
import org.openstreetmap.atlas.tags.oneway.motor.OneWayVehicleTag;

/**
 * OSM's oneway tag
 *
 * @author cstaylor
 * @author matthieun
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/oneway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:oneway")
public enum OneWayTag
{
    YES,
    NO,
    REVERSIBLE,
    @TagValueDeprecated
    TRUE,
    @TagValueDeprecated
    FALSE,
    @TagValueDeprecated
    @TagValueAs("1")
    ONE,
    @TagValueDeprecated
    @TagValueAs(value = "0")
    ZERO,
    @TagValueAs("-1")
    MINUS_1,
    @TagValueDeprecated
    REVERSE;

    @TagKey
    public static final String KEY = "oneway";

    protected static final Set<OneWayTag> ONE_WAYS_FORWARD = EnumSet.of(YES, TRUE, ONE);
    // Note here that REVERSIBLE is not reversed.
    protected static final Set<OneWayTag> ONE_WAYS_REVERSED = EnumSet.of(MINUS_1, REVERSE);
    protected static final Set<OneWayTag> TWO_WAYS = EnumSet.of(NO, FALSE, ZERO);

    /**
     * @param taggable
     *            The taggable
     * @return True if the feature is oneway forward, OR bicycle oneway forward.
     */
    public static boolean isBicycleOneWayForward(final Taggable taggable)
    {
        return isOneWayForward(taggable) || isBicycleTagSpecificallyOneWayForward(taggable);
    }

    /**
     * @param taggable
     *            The taggable
     * @return True if the feature is oneway reverse, OR bicycle oneway reverse.
     */
    public static boolean isBicycleOneWayReversed(final Taggable taggable)
    {
        return isOneWayReversed(taggable) || isBicycleTagSpecificallyOneWayReversed(taggable);
    }

    /**
     * @param taggable
     *            The taggable
     * @return True if the feature is two way, AND bicycle two way.
     */
    public static boolean isBicycleTwoWay(final Taggable taggable)
    {
        return OneWayTag.isTwoWay(taggable) && isBicycleTagSpecificallyTwoWay(taggable);
    }

    /**
     * This is a subset of two way roads, in which the two way status has been tagged with oneway=no
     * and not just assumed because of the absence of a oneway tag.
     *
     * @param taggable
     *            The object to test
     * @return True if the object is explicitly two way
     */
    public static boolean isExplicitlyTwoWay(final Taggable taggable)
    {
        final Optional<OneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && TWO_WAYS.contains(oneWay.get());
    }

    public static boolean isMotorVehicleOneWayForward(final Taggable taggable)
    {
        return isOneWayForward(taggable) || OneWayMotorcarTag.isOneWayForward(taggable)
                || OneWayMotorVehicleTag.isOneWayForward(taggable)
                || OneWayVehicleTag.isOneWayForward(taggable);
    }

    public static boolean isMotorVehicleOneWayReversed(final Taggable taggable)
    {
        return isOneWayReversed(taggable) || OneWayMotorcarTag.isOneWayReversed(taggable)
                || OneWayMotorVehicleTag.isOneWayReversed(taggable)
                || OneWayVehicleTag.isOneWayReversed(taggable);
    }

    public static boolean isMotorVehicleTwoWay(final Taggable taggable)
    {
        // All the motor related tags need to be two way (including not set)
        return OneWayTag.isTwoWay(taggable) && OneWayMotorcarTag.isTwoWay(taggable)
                && OneWayMotorVehicleTag.isTwoWay(taggable) && OneWayVehicleTag.isTwoWay(taggable);
    }

    public static boolean isOneWayForward(final OneWayTag tag)
    {
        return ONE_WAYS_FORWARD.contains(tag);
    }

    public static boolean isOneWayForward(final Taggable taggable)
    {
        final Optional<OneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && ONE_WAYS_FORWARD.contains(oneWay.get());
    }

    public static boolean isOneWayReversed(final OneWayTag tag)
    {
        return ONE_WAYS_REVERSED.contains(tag);
    }

    public static boolean isOneWayReversed(final Taggable taggable)
    {
        final Optional<OneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && ONE_WAYS_REVERSED.contains(oneWay.get());
    }

    public static boolean isOneWayReversible(final OneWayTag tag)
    {
        return REVERSIBLE == tag;
    }

    public static boolean isOneWayReversible(final Taggable taggable)
    {
        final Optional<OneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && REVERSIBLE == oneWay.get();
    }

    public static boolean isTwoWay(final OneWayTag tag)
    {
        return TWO_WAYS.contains(tag);
    }

    public static boolean isTwoWay(final Taggable taggable)
    {
        final Optional<OneWayTag> oneWay = tag(taggable);
        return !oneWay.isPresent() || TWO_WAYS.contains(oneWay.get());
    }

    public static Optional<OneWayTag> tag(final Taggable taggable)
    {
        return Validators.from(OneWayTag.class, taggable);
    }

    private static boolean isBicycleTagSpecificallyOneWayForward(final Taggable taggable)
    {
        return BicycleOneWayTag.isOneWayForward(taggable)
                || CyclewayOneWayTag.isOneWayForward(taggable)
                || OneWayBicycleTag.isOneWayForward(taggable)
                || CyclewayRightOneWayTag.isOneWayForward(taggable)
                || CyclewayLeftOneWayTag.isOneWayForward(taggable);
    }

    private static boolean isBicycleTagSpecificallyOneWayReversed(final Taggable taggable)
    {
        return BicycleOneWayTag.isOneWayReversed(taggable)
                || CyclewayOneWayTag.isOneWayReversed(taggable)
                || OneWayBicycleTag.isOneWayReversed(taggable)
                || CyclewayRightOneWayTag.isOneWayReversed(taggable)
                || CyclewayLeftOneWayTag.isOneWayReversed(taggable);
    }

    private static boolean isBicycleTagSpecificallyTwoWay(final Taggable taggable)
    {
        return BicycleOneWayTag.isTwoWay(taggable) && CyclewayOneWayTag.isTwoWay(taggable)
                && OneWayBicycleTag.isTwoWay(taggable) && CyclewayRightOneWayTag.isTwoWay(taggable)
                && CyclewayLeftOneWayTag.isTwoWay(taggable);
    }
}

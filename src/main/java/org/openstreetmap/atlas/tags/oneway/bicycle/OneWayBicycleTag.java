package org.openstreetmap.atlas.tags.oneway.bicycle;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValueAs;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * @author matthieun
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/oneway%3Abicycle#values", osm = "http://wiki.openstreetmap.org/wiki/Key:oneway")
public enum OneWayBicycleTag
{
    YES,
    NO,
    @TagValueAs("1")
    ONE,
    @TagValueAs("-1")
    MINUS_1,
    OPPOSITE;

    @TagKey
    public static final String KEY = "oneway:bicycle";

    protected static final Set<OneWayBicycleTag> ONE_WAYS_FORWARD = EnumSet.of(YES, ONE);
    protected static final Set<OneWayBicycleTag> ONE_WAYS_REVERSED = EnumSet.of(OPPOSITE, MINUS_1);
    protected static final Set<OneWayBicycleTag> TWO_WAYS = EnumSet.of(NO);

    public static boolean isExplicitlyTwoWay(final Taggable taggable)
    {
        final Optional<OneWayBicycleTag> oneWay = tag(taggable);
        return oneWay.isPresent() && TWO_WAYS.contains(oneWay.get());
    }

    public static boolean isOneWayForward(final Taggable taggable)
    {
        final Optional<OneWayBicycleTag> oneWay = tag(taggable);
        return oneWay.isPresent() && ONE_WAYS_FORWARD.contains(oneWay.get());
    }

    public static boolean isOneWayReversed(final Taggable taggable)
    {
        final Optional<OneWayBicycleTag> oneWay = tag(taggable);
        return oneWay.isPresent() && ONE_WAYS_REVERSED.contains(oneWay.get());
    }

    public static boolean isTwoWay(final Taggable taggable)
    {
        final Optional<OneWayBicycleTag> oneWay = tag(taggable);
        return oneWay.isEmpty() || TWO_WAYS.contains(oneWay.get());
    }

    public static Optional<OneWayBicycleTag> tag(final Taggable taggable)
    {
        return Validators.from(OneWayBicycleTag.class, taggable);
    }
}

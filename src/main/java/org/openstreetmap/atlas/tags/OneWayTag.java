package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValueAs;
import org.openstreetmap.atlas.tags.annotations.TagValueDeprecated;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

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

    public static final EnumSet<OneWayTag> ONE_WAYS_FORWARD = EnumSet.of(YES, TRUE, ONE);
    // Note here that REVERSIBLE is not reversed.
    public static final EnumSet<OneWayTag> ONE_WAYS_REVERSED = EnumSet.of(MINUS_1, REVERSE);
    public static final EnumSet<OneWayTag> TWO_WAYS = EnumSet.of(NO, FALSE, ZERO);

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
}

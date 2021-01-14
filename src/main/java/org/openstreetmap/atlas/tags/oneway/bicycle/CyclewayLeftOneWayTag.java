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
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/cycleway%3Aleft%3Aoneway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:oneway")
public enum CyclewayLeftOneWayTag
{
    YES,
    NO,
    @TagValueAs("1")
    ONE,
    @TagValueAs("-1")
    MINUS_1,
    LANE,
    OPPOSITE,
    FALSE;

    @TagKey
    public static final String KEY = "cycleway:left:oneway";

    protected static final Set<CyclewayLeftOneWayTag> ONE_WAYS_FORWARD = EnumSet.of(YES, ONE, LANE);
    protected static final Set<CyclewayLeftOneWayTag> ONE_WAYS_REVERSED = EnumSet.of(MINUS_1,
            OPPOSITE);
    protected static final Set<CyclewayLeftOneWayTag> TWO_WAYS = EnumSet.of(NO, FALSE);

    public static boolean isExplicitlyTwoWay(final Taggable taggable)
    {
        final Optional<CyclewayLeftOneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && TWO_WAYS.contains(oneWay.get());
    }

    public static boolean isOneWayForward(final Taggable taggable)
    {
        final Optional<CyclewayLeftOneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && ONE_WAYS_FORWARD.contains(oneWay.get());
    }

    public static boolean isOneWayReversed(final Taggable taggable)
    {
        final Optional<CyclewayLeftOneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && ONE_WAYS_REVERSED.contains(oneWay.get());
    }

    public static boolean isTwoWay(final Taggable taggable)
    {
        final Optional<CyclewayLeftOneWayTag> oneWay = tag(taggable);
        return oneWay.isEmpty() || TWO_WAYS.contains(oneWay.get());
    }

    public static Optional<CyclewayLeftOneWayTag> tag(final Taggable taggable)
    {
        return Validators.from(CyclewayLeftOneWayTag.class, taggable);
    }
}

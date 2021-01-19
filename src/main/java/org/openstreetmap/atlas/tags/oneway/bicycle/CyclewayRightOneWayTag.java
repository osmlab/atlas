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
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/cycleway%3Aright%3Aoneway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:oneway")
public enum CyclewayRightOneWayTag
{
    YES,
    NO,
    @TagValueAs("1")
    ONE,
    @TagValueAs("-1")
    MINUS_1,
    LANE,
    DESIGNATED;

    @TagKey
    public static final String KEY = "cycleway:right:oneway";

    protected static final Set<CyclewayRightOneWayTag> ONE_WAYS_FORWARD = EnumSet.of(YES, ONE, LANE,
            DESIGNATED);
    protected static final Set<CyclewayRightOneWayTag> ONE_WAYS_REVERSED = EnumSet.of(MINUS_1);
    protected static final Set<CyclewayRightOneWayTag> TWO_WAYS = EnumSet.of(NO);

    public static boolean isExplicitlyTwoWay(final Taggable taggable)
    {
        final Optional<CyclewayRightOneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && TWO_WAYS.contains(oneWay.get());
    }

    public static boolean isOneWayForward(final Taggable taggable)
    {
        final Optional<CyclewayRightOneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && ONE_WAYS_FORWARD.contains(oneWay.get());
    }

    public static boolean isOneWayReversed(final Taggable taggable)
    {
        final Optional<CyclewayRightOneWayTag> oneWay = tag(taggable);
        return oneWay.isPresent() && ONE_WAYS_REVERSED.contains(oneWay.get());
    }

    public static boolean isTwoWay(final Taggable taggable)
    {
        final Optional<CyclewayRightOneWayTag> oneWay = tag(taggable);
        return oneWay.isEmpty() || TWO_WAYS.contains(oneWay.get());
    }

    public static Optional<CyclewayRightOneWayTag> tag(final Taggable taggable)
    {
        return Validators.from(CyclewayRightOneWayTag.class, taggable);
    }
}

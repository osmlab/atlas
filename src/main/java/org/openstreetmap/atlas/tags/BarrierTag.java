package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM barrier tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/barrier#values", osm = "http://wiki.openstreetmap.org/wiki/Key:barrier")
public enum BarrierTag
{
    FENCE,
    WALL,
    GATE,
    HEDGE,
    BOLLARD,
    LIFT_GATE,
    RETAINING_WALL,
    STILE,
    CYCLE_BARRIER,
    KERB,
    YES,
    ENTRANCE,
    BLOCK,
    TOLL_BOOTH,
    CATTLE_GRID,
    DITCH,
    KISSING_GATE,
    CITY_WALL,
    GUARD_RAIL,
    HEDGE_BANK,
    WIRE_FENCE,
    LINE,
    SWING_GATE,
    CHAIN,
    TURNSTILE,
    EMBANKMENT,
    FIELD_BOUNDARY,
    BORDER_CONTROL,
    SALLY_PORT,
    DOOR,
    HAMPSHIRE_GATE,
    WOOD_FENCE,
    BUMP_GATE;

    @TagKey
    public static final String KEY = "barrier";

    private static final EnumSet<BarrierTag> BARRIERS = EnumSet.of(FENCE, WALL, GATE, HEDGE,
            BOLLARD, LIFT_GATE, RETAINING_WALL, STILE, CYCLE_BARRIER, KERB, YES, ENTRANCE, BLOCK,
            TOLL_BOOTH, CATTLE_GRID, DITCH, KISSING_GATE, CITY_WALL, GUARD_RAIL, HEDGE_BANK,
            WIRE_FENCE, LINE, SWING_GATE, CHAIN, TURNSTILE, EMBANKMENT, FIELD_BOUNDARY,
            BORDER_CONTROL, SALLY_PORT, DOOR, HAMPSHIRE_GATE, WOOD_FENCE, BUMP_GATE);

    public static boolean isBarrier(final Taggable taggable)
    {
        final Optional<BarrierTag> barrier = Validators.from(BarrierTag.class, taggable);
        return barrier.isPresent() && BARRIERS.contains(barrier.get());
    }
}

package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM's railway tag
 *
 * @author cstaylor
 * @author matthieun
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/railway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:railway")
public enum RailwayTag
{
    RAIL,
    LEVEL_CROSSING,
    ABANDONED,
    SWITCH,
    TRAVERSER,
    BUFFER_STOP,
    STATION,
    PLATFORM,
    TRAM,
    DISUSED,
    CROSSING,
    SIGNAL,
    TRAM_STOP,
    SUBWAY,
    HALT,
    NARROW_GAUGE,
    MILESTONE,
    SUBWAY_ENTRANCE,
    LIGHT_RAIL,
    STOP,
    PRESERVED,
    RAZED,
    CONSTRUCTION,
    RAILWAY_CROSSING,
    DISMANTLED,
    PROPOSED,
    DERAIL,
    MINIATURE,
    TURNTABLE,
    MONORAIL,
    FUNICULAR;

    @TagKey
    public static final String KEY = "railway";

    private static final EnumSet<RailwayTag> RAILWAY_CROSSINGS = EnumSet.of(CROSSING,
            LEVEL_CROSSING);

    public static Optional<RailwayTag> get(final Taggable taggable)
    {
        return Validators.from(RailwayTag.class, taggable);
    }

    public static boolean isRailway(final Taggable taggable)
    {
        return get(taggable).isPresent();
    }

    public static boolean isRailwayCrossing(final Taggable taggable)
    {
        final Optional<RailwayTag> railway = get(taggable);
        return railway.isPresent() && RAILWAY_CROSSINGS.contains(railway.get());
    }
}

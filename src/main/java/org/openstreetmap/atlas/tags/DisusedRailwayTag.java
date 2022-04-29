package org.openstreetmap.atlas.tags;

import java.util.EnumSet;
import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM disused: Railway tag
 *
 * @author Vladimir Lemberg
 */
@Tag(with = {
        ShopTag.class }, taginfo = "https://taginfo.openstreetmap.org/keys/disused%3Arailway#values", osm = "https://wiki.openstreetmap.org/wiki/Key:disused:railway")
public enum DisusedRailwayTag
{
    YES,
    RAIL,
    LIGHT_RAIL,
    CROSSING,
    LEVEL_CROSSING,
    STATION,
    HALT,
    PLATFORM,
    TRAM;

    @TagKey
    public static final String KEY = "disused:railway";

    private static final EnumSet<DisusedRailwayTag> DISUSED_RAILWAY_CROSSINGS = EnumSet.of(CROSSING,
            LEVEL_CROSSING);

    public static Optional<DisusedRailwayTag> get(final Taggable taggable)
    {
        return Validators.from(DisusedRailwayTag.class, taggable);
    }

    public static boolean isDisusedRailwayCrossing(final Taggable taggable)
    {
        final Optional<DisusedRailwayTag> disusedRailway = get(taggable);
        return disusedRailway.isPresent()
                && DISUSED_RAILWAY_CROSSINGS.contains(disusedRailway.get());
    }
}

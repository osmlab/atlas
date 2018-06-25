package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM leisure tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/leisure#values", osm = "http://wiki.openstreetmap.org/wiki/Key:leisure")
public enum LeisureTag
{
    PITCH,
    SWIMMING_POOL,
    PARK,
    PLAYGROUND,
    GARDEN,
    SPORTS_CENTRE,
    NATURE_RESERVE,
    COMMON,
    TRACK,
    STADIUM,
    GOLF_COURSE,
    RECREATION_GROUND,
    SLIPWAY,
    PICNIC_TABLE,
    MARINA,
    WATER_PARK,
    DOG_PARK,
    FIREPIT,
    SAUNA,
    MINIATURE_GOLF,
    FISHING,
    HORSE_RIDING,
    FITNESS_STATION,
    ICE_RINK,
    BEACH_RESORT,
    YES,
    BIRD_HIDE,
    RESORT,
    DANCE,
    ADULT_GAMING_CENTRE,
    CLUB,
    CLIMBING,
    BLEACHERS,
    OUTDOOR_SEATING,
    TANNING_SALON,
    BANDSTAND,
    SOCIAL_CLUB,
    FESTIVAL_GROUNDS;

    @TagKey
    public static final String KEY = "leisure";

    public static Optional<LeisureTag> get(final Taggable taggable)
    {
        return Validators.from(LeisureTag.class, taggable);
    }
}

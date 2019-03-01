package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM water tag
 *
 * @author Sid
 * @author cstaylor
 * @author mgostintsev
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/water#values", osm = "http://wiki.openstreetmap.org/wiki/Key:water")
public enum WaterTag
{
    INTERMITTENT,
    LAKE,
    LAGOON,
    POND,
    REFLECTING_POOL,
    RESERVOIR,
    BASIN,
    CANAL,
    RIVER,
    FISH_PASS,
    OXBOW,
    LOCK,
    MOAT,
    WASTEWATER,
    STREAM_POOL,
    SEA,
    TIDAL,
    SALT_POOL,
    POOL;

    @TagKey
    public static final String KEY = "water";

    public static Optional<WaterTag> get(final Taggable taggable)
    {
        return Validators.from(WaterTag.class, taggable);
    }
}

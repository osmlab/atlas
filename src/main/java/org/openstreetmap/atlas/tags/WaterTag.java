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
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/water#values", osm = "http://wiki.openstreetmap.org/wiki/Key:water")
public enum WaterTag
{
    INTERMITTENT,
    POND,
    LAKE,
    RESERVOIR,
    RIVER,
    CANAL,
    TIDAL,
    SALT_POOL,
    LAGOON,
    POOL;

    @TagKey
    public static final String KEY = "water";

    public static Optional<WaterTag> get(final Taggable taggable)
    {
        return Validators.from(WaterTag.class, taggable);
    }
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM military tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/military#values", osm = "https://wiki.openstreetmap.org/wiki/Key%3Amilitary")
public enum MilitaryTag
{
    BUNKER,
    BARRACKS,
    NUCLEAR_EXPLOSION_SITE,
    RANGE,
    TRENCH,
    AIRFIELD,
    YES,
    CHECKPOINT,
    DANGER_AREA,
    NAVAL_BASE,
    TRAINING_AREA;

    @TagKey
    public static final String KEY = "military";
}

package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM waterway tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/waterway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:waterway")
public enum WaterwayTag
{
    STREAM,
    DITCH,
    RIVER,
    DRAIN,
    RIVERBANK,
    CANAL,
    DAM,
    WEIR,
    RAPIDS,
    WATERFALL,
    LOCK_GATE,
    WADI,
    DRYSTREAM,
    DOCK,
    BOATYARD,
    DERELICT_CANAL,
    MILESTONE,
    BROOK,
    TURNING_POINT,
    FUEL,
    FISH_PASS,
    WATER_POINT;

    @TagKey
    public static final String KEY = "waterway";

    public static Optional<WaterwayTag> get(final Taggable taggable)
    {
        return Validators.from(WaterwayTag.class, taggable);
    }
}

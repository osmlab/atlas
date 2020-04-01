package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM aerialway tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/aerialway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:aerialway")
public enum AerialWayTag
{
    CABLE_CAR,
    GONDOLA,
    CHAIR_LIFT,
    MIXED_LIFT,
    DRAG_LIFT,
    T_BAR,
    J_BAR,
    PLATTER,
    ROPE_TOW,
    MAGIC_CARPET,
    ZIP_LINE,
    PYLON,
    STATION;

    @TagKey
    public static final String KEY = "aerialway";
}

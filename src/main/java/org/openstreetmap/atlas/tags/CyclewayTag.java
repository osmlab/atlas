package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM cycleway tag
 *
 * @author robert_stack
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/cycleway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:cycleway")
public enum CyclewayTag
{
    LANE,
    OPPOSITE_LANE,
    OPPOSITE,
    SHARED_LANE,
    SHARE_BUSWAY,
    SHARED,
    TRACK,
    OPPOSITE_TRACK,
    ASL,
    SHOULDER,
    NO,
    YES,
    LEFT,
    RIGHT,
    OPPOSITE_SHARE_BUSWAY,
    SEGREGATED,
    NONE,
    SEPARATE;

    @TagKey
    public static final String KEY = "cycleway";
}

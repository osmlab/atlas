package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM cycleway right tag
 *
 * @author james_gage
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/cycleway%3Aright#values", osm = "http://wiki.openstreetmap.org/wiki/Key:cycleway:right")
public enum CyclewayRightTag
{
    LANE,
    TRACK,
    OPPOSITE_LANE,
    OPPOSITE_SHARE_BUSWAY;

    @TagKey
    public static final String KEY = "cycleway:right";
}

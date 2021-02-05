package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM cycleway left tag
 *
 * @author james_gage
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/cycleway%3Aleft#values", osm = "http://wiki.openstreetmap.org/wiki/Key:cycleway:left")
public enum CyclewayLeftTag
{
    LANE,
    TRACK,
    OPPOSITE_LANE,
    OPPOSITE_SHARE_BUSWAY;

    @TagKey
    public static final String KEY = "cycleway:left";
}

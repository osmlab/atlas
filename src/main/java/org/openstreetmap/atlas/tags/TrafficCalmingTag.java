package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM traffic_calming tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "http://wiki.openstreetmap.org/wiki/Key:traffic_calming#Common_values", osm = "http://wiki.openstreetmap.org/wiki/Key:traffic_calming")
public enum TrafficCalmingTag
{
    BUMP,
    HUMP,
    TABLE,
    CUSHION,
    RUMBLE_STRIP,
    CHICANE,
    CHOKER,
    ISLAND;

    @TagKey
    public static final String KEY = "traffic_calming";
}

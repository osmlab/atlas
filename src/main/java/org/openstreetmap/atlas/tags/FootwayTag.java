package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM footway tag
 *
 * @author robert_stack
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/footway#values", osm = "http://wiki.openstreetmap.org/wiki/Key:footway")
public enum FootwayTag
{
    SIDEWALK,
    CROSSING;

    @TagKey
    public static final String KEY = "footway";
}

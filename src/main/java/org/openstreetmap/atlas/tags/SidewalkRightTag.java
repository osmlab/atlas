package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM sidewalk right tag
 *
 * @author Vladimir Lemberg
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/sidewalk:right#values", osm = "https://wiki.openstreetmap.org/wiki/Key:sidewalk:right")
public enum SidewalkRightTag
{
    NO,
    YES,
    SEPARATE;

    @TagKey
    public static final String KEY = "sidewalk:right";
}

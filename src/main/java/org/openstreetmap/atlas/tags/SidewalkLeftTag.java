package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM sidewalk left tag
 *
 * @author Vladimir Lemberg
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/sidewalk:left#values", osm = "https://wiki.openstreetmap.org/wiki/Key:sidewalk:left")
public enum SidewalkLeftTag
{
    NO,
    YES,
    SEPARATE;

    @TagKey
    public static final String KEY = "sidewalk:left";
}

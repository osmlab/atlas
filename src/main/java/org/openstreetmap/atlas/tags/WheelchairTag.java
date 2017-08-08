package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM wheelchair tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/wheelchair#values", osm = "http://wiki.openstreetmap.org/wiki/Key%3Awheelchair")
public enum WheelchairTag
{
    YES,
    NO,
    LIMITED;

    @TagKey
    public static final String KEY = "wheelchair";
}

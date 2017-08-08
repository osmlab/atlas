package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM smoking tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/smoking#values", osm = "http://wiki.openstreetmap.org/wiki/Key:smoking")
public enum SmokingTag
{
    NO,
    OUTSIDE,
    YES,
    SEPARATED,
    ISOLATED,
    DEDICATED,
    UNKNOWN;

    @TagKey
    public static final String KEY = "smoking";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM salt tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/salt#values", osm = "http://wiki.openstreetmap.org/wiki/Key:salt")
public enum SaltTag
{
    YES,
    NO,
    UNKNOWN;

    @TagKey
    public static final String KEY = "salt";
}

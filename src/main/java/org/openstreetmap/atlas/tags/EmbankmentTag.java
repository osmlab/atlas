package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM embankment tag
 *
 * @author mkalender
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/embankment#values", osm = "http://wiki.openstreetmap.org/wiki/Key:embankment")
public enum EmbankmentTag
{
    YES,
    NO;

    @TagKey
    public static final String KEY = "embankment";
}

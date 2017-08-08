package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM internet_access:fee tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/internet_access:fee#values", osm = "http://wiki.openstreetmap.org/wiki/Key:internet_access")
public enum InternetAccessFeeTag
{
    NO,
    YES;

    @TagKey
    public static final String KEY = "internet_access:fee";
}

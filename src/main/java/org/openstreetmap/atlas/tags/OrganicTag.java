package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM organic tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/organic#values", osm = "http://wiki.openstreetmap.org/wiki/Key:organic")
public enum OrganicTag
{
    ONLY,
    YES,
    NO,
    LIMITED;

    @TagKey
    public static final String KEY = "organic";
}

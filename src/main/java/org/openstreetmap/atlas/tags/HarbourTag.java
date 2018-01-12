package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Harbour tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/harbour#values", osm = "http://wiki.openstreetmap.org/wiki/Harbour")
public enum HarbourTag
{
    YES;

    @TagKey
    public static final String KEY = "harbour";
}

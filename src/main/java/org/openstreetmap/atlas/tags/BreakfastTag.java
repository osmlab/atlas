package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM breakfast tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/breakfast#values", osm = "http://wiki.openstreetmap.org/wiki/Key:breakfast")
public enum BreakfastTag
{
    YES,
    BUFFET,
    NO,
    FREE;

    @TagKey
    public static final String KEY = "breakfast";
}

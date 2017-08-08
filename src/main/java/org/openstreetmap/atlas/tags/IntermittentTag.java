package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM's intermittent tags
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/intermittent#values", osm = "http://wiki.openstreetmap.org/wiki/Key:intermittent")
public enum IntermittentTag
{
    YES,
    NO,
    DRY;

    @TagKey
    public static final String KEY = "intermittent";
}

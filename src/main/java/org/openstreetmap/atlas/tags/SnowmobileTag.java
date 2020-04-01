package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Snowmobile Tag.
 *
 * @author sayas01
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/snowmobile#values", osm = "https://wiki.openstreetmap.org/wiki/Key:snowmobile")
public enum SnowmobileTag
{
    NO,
    DESIGNATED,
    YES,
    PERMISSIVE,
    PRIVATE;

    @TagKey
    public static final String KEY = "snowmobile";
}

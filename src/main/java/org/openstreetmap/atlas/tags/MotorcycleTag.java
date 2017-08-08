package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM motorcyle tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/motorcycle#values", osm = "http://wiki.openstreetmap.org/wiki/Key:motorcycle")
public enum MotorcycleTag
{
    NO,
    YES,
    AGRICULTURAL,
    DESIGNATED,
    FORESTRY,
    PRIVATE,
    PERMISSIVE,
    UNKNOWN;

    @TagKey
    public static final String KEY = "motorcycle";
}

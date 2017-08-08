package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM bicycle tag. Also see http://wiki.openstreetmap.org/wiki/Bicycle#Bicycle_Restrictions for
 * further tagging detail.
 *
 * @author robert_stack
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/bicycle#values", osm = "http://wiki.openstreetmap.org/wiki/Key:bicycle")
public enum BicycleTag
{
    YES,
    DESIGNATED,
    USE_SIDEPATH,
    NO,
    PERMISSIVE,
    DESTINATION,
    DISMOUNT,
    PRIVATE,
    OFFICIAL,
    UNKNOWN;

    @TagKey
    public static final String KEY = "bicycle";
}

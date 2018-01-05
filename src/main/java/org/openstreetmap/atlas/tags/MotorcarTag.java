package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM motorcar tag
 *
 * @author mkalender
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/motorcar#values", osm = "http://wiki.openstreetmap.org/wiki/Key:motorcar")
public enum MotorcarTag
{
    YES,
    NO,
    PRIVATE,
    AGRICULTURAL,
    PERMISSIVE,
    DESTINATION,
    DESIGNATED,
    DELIVERY,
    FORESTRY,
    OFFICIAL,
    CUSTOMERS,
    EMERGENCY,
    BUS,
    SERVICE,
    UNKNOWN;

    @TagKey
    public static final String KEY = "motorcar";
}

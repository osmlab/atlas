package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Ski Tag.
 *
 * @author sayas01
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/ski#values", osm = "https://wiki.openstreetmap.org/wiki/Key:ski")
public enum SkiTag
{
    NO,
    YES,
    DESIGNATED,
    OFFICIAL,
    PERMISSIVE,
    CROSSING,
    PRIVATE,
    DOWNHILL,
    CUSTOMERS,
    NORDIC;

    @TagKey
    public static final String KEY = "ski";
}

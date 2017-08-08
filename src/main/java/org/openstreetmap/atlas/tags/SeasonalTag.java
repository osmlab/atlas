package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM seasonal tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/seasonal#values", osm = "http://wiki.openstreetmap.org/wiki/Key:seasonal")
public enum SeasonalTag
{
    NO,
    YES,
    DRY_SEASON,
    WET_SEASON,
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER;

    @TagKey
    public static final String KEY = "seasonal";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM entrance tag
 *
 * @author savannahostrowski
 */
@Tag(taginfo = "https://wiki.openstreetmap.org/wiki/Key:entrance#values", osm = "https://wiki.openstreetmap.org/wiki/Key:entrance")
public enum EntranceTag
{
    EMERGENCY,
    EXIT,
    HOME,
    MAIN,
    SERVICE,
    STAIRCASE,
    YES;

    @TagKey
    public static final String KEY = "entrance";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM Winter Tag.
 *
 * @author sayas01
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/winter_road#values", osm = "https://wiki.openstreetmap.org/wiki/Key:winter_road")
public enum WinterRoadTag
{
    YES,
    NO;

    @TagKey
    public static final String KEY = "winter_road";
}

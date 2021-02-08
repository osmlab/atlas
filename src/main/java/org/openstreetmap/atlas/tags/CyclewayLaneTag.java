package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM cycleway lane tag
 *
 * @author james_gage
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/cycleway%3Alane#values", osm = "http://wiki.openstreetmap.org/wiki/Key:cycleway:lane")
public enum CyclewayLaneTag
{
    ADVISORY,
    EXCLUSIVE;

    @TagKey
    public static final String KEY = "cycleway:lane";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM hires tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/hires#values")
public enum HighResolutionTag
{
    YES,
    NO,
    MAPBOX,
    BING;

    @TagKey
    public static final String KEY = "hires";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM wetland tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/wetland#values", osm = "http://wiki.openstreetmap.org/wiki/Key:wetland")
public enum WetlandTag
{
    BOG,
    MARSH,
    SWAMP,
    REEDBED,
    TIDALFLAT,
    MANGROVE,
    WET_MEADOW,
    SALTMARSH,
    STRING_BOG,
    SALTERN,
    FEN;

    @TagKey
    public static final String KEY = "wetland";
}

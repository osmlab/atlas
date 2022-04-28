package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM disused: Railway tag
 *
 * @author Vladimir Lemberg
 */
@Tag(with = {
        ShopTag.class }, taginfo = "https://taginfo.openstreetmap.org/keys/disused%3Arailway#values", osm = "https://wiki.openstreetmap.org/wiki/Key:disused:railway")
public enum DisusedRailwayTag
{
    YES,
    RAIL,
    LIGHT_RAIL,
    CROSSING,
    LEVEL_CROSSING,
    STATION,
    HALT,
    PLATFORM,
    TRAM;

    @TagKey
    public static final String KEY = "disused:railway";
}

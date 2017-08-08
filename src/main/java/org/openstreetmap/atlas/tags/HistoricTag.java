package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM historic tag
 *
 * @author mgostintsev
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/historic#values", osm = "http://wiki.openstreetmap.org/wiki/Key:historic")
public enum HistoricTag
{
    AIRCRAFT,
    ARCHAEOLOGICAL_SITE,
    BATTLEFIELD,
    BOUNDARY_STONE,
    BUILDING,
    CANNON,
    CASTLE,
    CITY_GATE,
    CITYWALLS,
    FARM,
    FORT,
    GALLOWS,
    LOCOMOTIVE,
    MANOR,
    MEMORIAL,
    MILESTONE,
    MONASTERY,
    MONUMENT,
    OPTICAL_TELEGRAPH,
    PILLORY,
    RUINS,
    RUNE_STONE,
    SHIP,
    TOMB,
    WAYSIDE_CROSS,
    WAYSIDE_SHRINE,
    WRECK;

    @TagKey
    public static final String KEY = "historic";
}

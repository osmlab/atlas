package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM amenity tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/amenity#values", osm = "http://wiki.openstreetmap.org/wiki/Key:amenity")
public enum AmenityTag
{
    PARKING,
    PARKING_ENTRANCE,
    MOTORCYCLE_PARKING,
    PLACE_OF_WORSHIP,
    SCHOOL,
    BENCH,
    RESTAURANT,
    FUEL,
    CAFE,
    FAST_FOOD,
    BANK,
    POST_BOX,
    GRAVE_YARD,
    KINDERGARTEN,
    RECYCLING,
    PHARMACY,
    WASTE_BASKET,
    BICYCLE_PARKING,
    TOILETS,
    HOSPITAL,
    SHELTER,
    POST_OFFICE,
    PUB,
    DRINKING_WATER,
    PUBLIC_BUILDING,
    TELEPHONE,
    ATM,
    BAR,
    POLICE,
    FIRE_STATION,
    TOWNHALL,
    HUNTING_STAND,
    PARKING_SPACE,
    VENDING_MACHINE,
    FOUNTAIN,
    LIBRARY,
    DOCTORS,
    SWIMMING_POOL,
    SOCIAL_FACILITY,
    UNIVERSITY,
    BICYCLE_RENTAL,
    EMERGENCY_PHONE,
    WASTE_DISPOSAL,
    FESTIVAL_GROUNDS,
    COLLEGE,
    COMMUNITY_CENTRE,
    COMMUNITY_CENTER,
    MARKETPLACE,
    FERRY_TERMINAL;

    @TagKey
    public static final String KEY = "amenity";
}

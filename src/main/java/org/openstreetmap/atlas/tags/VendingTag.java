package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM's vending tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/vending#values", osm = "http://wiki.openstreetmap.org/wiki/Tag:amenity%3Dvending_machine")
public enum VendingTag
{
    PARKING_TICKETS,
    CIGARETTES,
    EXCREMENT_BAGS,
    PUBLIC_TRANSPORT_TICKETS,
    DRINKS,
    SWEETS,
    PARCEL_PICKUP,
    PARCEL_MAIL_IN,
    CONDOMS,
    NEWS_PAPERS,
    STAMPS,
    BICYCLE_TUBE,
    FUEL,
    GAS;

    @TagKey
    public static final String KEY = "vending";
}

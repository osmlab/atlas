package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM shop tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/shop#values", osm = "http://wiki.openstreetmap.org/wiki/Key:shop")
public enum ShopTag
{
    CONVENIENCE,
    SUPERMARKET,
    CLOTHES,
    HAIRDRESSER,
    BAKERY,
    CAR_REPAIR,
    CAR,
    YES,
    KIOSK,
    DOITYOURSELF,
    BUTCHER,
    FLORIST,
    MALL,
    FURNITURE,
    SHOES,
    BICYCLE,
    ALCOHOL,
    ELECTRONICS,
    HARDWARE,
    BOOKS,
    BEAUTY,
    MOBILE_PHONE,
    JEWELRY,
    DEPARTMENT_STORE,
    OPTICIAN,
    GIFT,
    GREENGROCER,
    CAR_PARTS,
    CHEMIST,
    VARIETY_STORE,
    SPORTS,
    GARDEN_CENTRE,
    COMPUTER,
    STATIONERY,
    TRAVEL_AGENCY,
    LAUNDRY,
    CONFECTIONERY,
    BEVERAGES,
    DRY_CLEANING,
    TOYS,
    TAILOR,
    ART,
    BABY_GOODS,
    BATHROOM_FURNISHING,
    BOUTIQUE,
    CARPET,
    CHEESE,
    CHOCOLATE,
    COPY_SHOP,
    COSMETICS,
    DAIRY,
    DELI,
    FABRIC,
    FARM,
    FASHION,
    VACANT;

    @TagKey
    public static final String KEY = "shop";

    public String getTagValue()
    {
        return name().toLowerCase().intern();
    }
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM tourism tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/tourism#values", osm = "http://wiki.openstreetmap.org/wiki/Key:tourism")
public enum TourismTag
{
    INFORMATION,
    HOTEL,
    ATTRACTION,
    VIEWPOINT,
    PICNIC_SITE,
    CAMP_SITE,
    GUEST_HOUSE,
    MUSEUM,
    ARTWORK,
    CHALET,
    MOTEL,
    HOSTEL,
    CARAVAN_SITE,
    ALPINE_HUT,
    THEME_PARK,
    ZOO,
    YES,
    APARTMENT,
    WILDERNESS_HUT,
    GALLERY,
    BED_AND_BREAKFAST,
    WINE_CELLAR,
    RESORT,
    AQUARIUM;

    @TagKey
    public static final String KEY = "tourism";
}

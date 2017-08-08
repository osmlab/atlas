package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM place tag
 *
 * @author robert_stack
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/place#values", osm = "http://wiki.openstreetmap.org/wiki/Key:place")
public enum PlaceTag
{
    COUNTRY,
    STATE,
    REGION,
    PROVINCE,
    DISTRICT,
    COUNTY,
    MUNICIPALITY,
    CITY,
    BOROUGH,
    SUBURB,
    QUARTER,
    NEIGHBOURHOOD,
    CITY_BLOCK,
    PLOT,
    TOWN,
    VILLAGE,
    HAMLET,
    ISOLATED_DWELLING,
    FARM,
    ALLOTMENTS,
    CONTINENT,
    ARCHIPELAGO,
    ISLAND,
    ISLET,
    LOCALITY,
    SQUARE,
    OCEAN,
    SEA;

    @TagKey
    public static final String KEY = "place";
}

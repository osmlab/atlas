package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM building:part tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/building%3Apart#values", osm = "http://wiki.openstreetmap.org/wiki/Key:building:part")
public enum BuildingPartTag
{
    YES,
    ROOF,
    APARTMENTS,
    COLUMN,
    NO,
    COMMERCIAL,
    BASE,
    STEPS,
    RESIDENTIAL,
    GARAGE,
    HOUSE,
    RETAIL,
    SCHOOL,
    ROOT,
    INDUSTRIAL,
    DEFAULT,
    STILOBATE,
    OFFICE,
    SUPERSTRUCTURE,
    RUINS,
    OUTLINE,
    UNIVERSITY;

    @TagKey
    public static final String KEY = "building:part";

    public String getTagValue()
    {
        return name().toLowerCase().intern();
    }
}

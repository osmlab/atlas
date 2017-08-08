package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM vacant tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/vacant#values")
public enum VacantTag
{
    YES,
    SELL,
    RENT,
    RESTAURANT,
    RENTAL,
    NO,
    PUB,
    CONSTRUCTION,
    SHOP;

    @TagKey
    public static final String KEY = "vacant";
}

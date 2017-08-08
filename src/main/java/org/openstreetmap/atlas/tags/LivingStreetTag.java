package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM's living_street tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/living_street#values", osm = "http://wiki.openstreetmap.org/wiki/Proposed_features/Tag:living_street%3Dyes")
public enum LivingStreetTag
{
    YES,
    NO,
    RESIDENTIAL;

    @TagKey
    public static final String KEY = "living_street";
}

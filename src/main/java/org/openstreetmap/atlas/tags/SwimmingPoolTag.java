package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM swimming_pool tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/swimming_pool#values")
public enum SwimmingPoolTag
{
    NO,
    YES,
    ABOVE,
    WADING,
    ENTRANCE,
    PLUNGE;

    @TagKey
    public static final String KEY = "swimming_pool";
}

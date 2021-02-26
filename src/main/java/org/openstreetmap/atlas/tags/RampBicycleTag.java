package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM ramp bicycle tag
 *
 * @author james_gage
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/ramp:bicycle#values", osm = "http://wiki.openstreetmap.org/wiki/Key:ramp:bicycle")
public enum RampBicycleTag
{
    YES,
    NO;

    @TagKey
    public static final String KEY = "ramp:bicycle";
}

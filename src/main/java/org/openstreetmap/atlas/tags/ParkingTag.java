package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValueAs;

/**
 * OSM parking tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/parking#values", osm = "http://wiki.openstreetmap.org/wiki/Key:parking")
public enum ParkingTag
{
    SURFACE,
    UNDERGROUND,
    @TagValueAs("multi-storey")
    MULTI_STOREY;

    @TagKey
    public static final String KEY = "parking";
}

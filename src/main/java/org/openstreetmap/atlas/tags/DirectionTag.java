package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM direction tag
 *
 * @author MonicaBrandeis
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/direction", osm = "https://wiki.openstreetmap.org/wiki/Key:direction")
public enum DirectionTag
{
    FORWARD,
    BACKWARD,
    CLOCKWISE,
    ANTICLOCKWISE,
    BOTH;

    @TagKey
    public static final String KEY = "direction";
}

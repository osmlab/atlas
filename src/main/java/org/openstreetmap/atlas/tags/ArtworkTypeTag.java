package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM artwork_type tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/artwork_type#values", osm = "http://wiki.openstreetmap.org/wiki/Key:artwork_type")
public enum ArtworkTypeTag
{
    SCULPTURE,
    STATUE,
    MURAL,
    ARCHITECTURE,
    STONE,
    PAINTING,
    BUST,
    INSTALLATION,
    MOSAIC;

    @TagKey
    public static final String KEY = "artwork_type";
}

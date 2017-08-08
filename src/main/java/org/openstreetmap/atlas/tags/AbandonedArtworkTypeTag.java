package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM abandoned:artwork_type tag
 *
 * @author cstaylor
 */
@Tag(with = {
        ArtworkTypeTag.class }, taginfo = "http://taginfo.openstreetmap.org/keys/abandoned%3Aartwork_type#values", osm = "https://wiki.openstreetmap.org/wiki/Key:abandoned:")
public interface AbandonedArtworkTypeTag
{
    @TagKey
    String KEY = "abandoned:artwork_type";
}

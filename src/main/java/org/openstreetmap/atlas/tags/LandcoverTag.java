package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM landcover tag
 *
 * @author stephencerqueira
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/landcover#values", osm = "https://wiki.openstreetmap.org/wiki/Proposed_features/landcover")
public enum LandcoverTag
{
    GRASS,
    GRAVEL,
    TREES;

    @TagKey
    public static final String KEY = "landcover";
}

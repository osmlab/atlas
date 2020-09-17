package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM construction:date tag
 *
 * @author brianjor
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/construction:date#values", osm= "https://wiki.openstreetmap.org/wiki/Item:Q9553")
public interface ConstructionDateTag
{
    @TagKey
    String KEY = "construction:date";
}

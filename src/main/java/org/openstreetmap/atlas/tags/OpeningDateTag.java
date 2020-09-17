package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM opening_date tag
 *
 * @author brianjor
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/opening_date#values", osm= "https://wiki.openstreetmap.org/wiki/Key:opening_date")
public interface OpeningDateTag
{
    @TagKey
    String KEY = "opening_date";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM check_date tag
 *
 * @author brianjor
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/check_date#values", osm= "https://wiki.openstreetmap.org/wiki/Key:check_date")
public interface CheckDateTag
{
    @TagKey
    String KEY = "check_date";
}

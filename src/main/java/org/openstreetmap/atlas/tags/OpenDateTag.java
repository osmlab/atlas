package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM open_date tag
 *
 * @author brianjor
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/open_date#values")
public interface OpenDateTag
{
    @TagKey
    String KEY = "open_date";
}

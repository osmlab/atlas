package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM temporary:date_on tag
 *
 * @author brianjor
 */
@Tag(taginfo = "https://taginfo.openstreetmap.org/keys/temporary:date_on#values", osm= "https://wiki.openstreetmap.org/wiki/Item:Q15233")
public interface TemporaryDateOnTag
{
    @TagKey
    String KEY = "temporary:date_on";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM abandoned:aeroway tag
 *
 * @author cstaylor
 */
@Tag(with = {
        AerowayTag.class }, taginfo = "http://taginfo.openstreetmap.org/keys/abandoned%3Aaeroway#values", osm = "https://wiki.openstreetmap.org/wiki/Key:abandoned:")
public interface AbandonedAerowayTag
{
    @TagKey
    String KEY = "abandoned:aeroway";
}

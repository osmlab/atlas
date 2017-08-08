package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM name_1 tag, not official per OSM tagging wiki, but is very prevalent especially in the USA
 *
 * @author brian_l_davis
 */
@Tag(value = Tag.Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/name_1#values", osm = "http://wiki.openstreetmap.org/wiki/Key:name_1")
public interface Name1Tag
{
    @TagKey
    String KEY = "name_1";
}

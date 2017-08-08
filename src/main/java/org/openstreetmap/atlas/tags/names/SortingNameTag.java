package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM sorting_name tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/sorting_name#values", osm = "http://wiki.openstreetmap.org/wiki/Key:sorting_name")
public interface SortingNameTag
{
    @TagKey
    String KEY = "sorting_name";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM addr:flats tag
 *
 * @author mgostintsev
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/search?q=addr%3Aflats", osm = "http://wiki.openstreetmap.org/wiki/Key:addr:flats")
public interface AddressFlatsTag
{
    @TagKey
    String KEY = "addr:flats";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM addr:housenumber tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/addr%3Ahousenumber#values", osm = "http://wiki.openstreetmap.org/wiki/Key:addr")
public interface AddressHousenumberTag
{
    @TagKey
    String KEY = "addr:housenumber";
}

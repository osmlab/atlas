package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM addr:postcode tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/addr%3Apostcode#values", osm = "http://wiki.openstreetmap.org/wiki/Key:addr")
public interface AddressPostcodeTag
{
    @TagKey
    String KEY = "addr:postcode";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM addr:country tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.ISO2_COUNTRY, taginfo = "http://taginfo.openstreetmap.org/keys/addr%3Acountry#values", osm = "http://wiki.openstreetmap.org/wiki/Key:addr")
public interface AddressCountryTag
{
    @TagKey
    String KEY = "addr:country";
}

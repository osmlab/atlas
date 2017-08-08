package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagKey.KeyType;

/**
 * OSM addr:housename tag
 *
 * @author cstaylor
 */

@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/addr%3Ahousename#values", osm = "http://wiki.openstreetmap.org/wiki/Key:addr")
public interface AddressHousenameTag
{
    @TagKey(KeyType.LOCALIZED)
    String KEY = "addr:housename";
}

package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM reg_name tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/reg_name#values", osm = "http://wiki.openstreetmap.org/wiki/Key:name")
public interface RegionallyKnownAsTag
{
    @TagKey
    String KEY = "reg_name";
}

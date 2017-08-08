package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM loc_name tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/loc_name#values", osm = "http://wiki.openstreetmap.org/wiki/Key:name")
public interface LocallyKnownAsTag
{
    @TagKey
    String KEY = "loc_name";
}

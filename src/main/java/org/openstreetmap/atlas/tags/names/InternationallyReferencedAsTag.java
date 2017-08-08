package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM int_ref tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/int_ref#values", osm = "http://wiki.openstreetmap.org/wiki/Key:int_ref")
public interface InternationallyReferencedAsTag
{
    @TagKey
    String KEY = "int_ref";
}

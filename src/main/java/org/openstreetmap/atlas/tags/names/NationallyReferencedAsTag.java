package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM nat_ref tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/nat_ref#values", osm = "http://wiki.openstreetmap.org/wiki/Key:ref#Examples_on_ways")
public interface NationallyReferencedAsTag
{
    @TagKey
    String KEY = "nat_ref";
}

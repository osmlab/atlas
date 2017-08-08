package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM cuisine tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/cuisine#values", osm = "http://wiki.openstreetmap.org/wiki/Key:cuisine")
public interface CuisineTag
{
    @TagKey
    String KEY = "cuisine";
}

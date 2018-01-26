package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM min_height tag: https://taginfo.openstreetmap.org/keys/min_height#values
 *
 * @author ajayaswal
 */
@Tag(value = Validation.LENGTH, taginfo = "https://taginfo.openstreetmap.org/keys/min_height#values", osm = "https://wiki.openstreetmap.org/wiki/Key:min_height")
public interface MinHeightTag
{
    @TagKey
    String KEY = "min_height";
}

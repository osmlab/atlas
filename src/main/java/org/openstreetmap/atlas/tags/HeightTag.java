package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM height tag: http://taginfo.openstreetmap.org/keys/height#values
 *
 * @author cstaylor
 */
@Tag(value = Validation.LENGTH, taginfo = "http://taginfo.openstreetmap.org/keys/height#values", osm = "http://wiki.openstreetmap.org/wiki/Key:height")
public interface HeightTag
{
    @TagKey
    String KEY = "height";
}

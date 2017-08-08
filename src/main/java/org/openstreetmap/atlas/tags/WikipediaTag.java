package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagKey.KeyType;

/**
 * OSM wikipedia tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/wikipedia#values", osm = "http://wiki.openstreetmap.org/wiki/Key:wikipedia")
public interface WikipediaTag
{
    @TagKey(KeyType.LOCALIZED)
    String KEY = "wikipedia";
}

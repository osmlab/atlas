package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagKey.KeyType;

/**
 * OSM name:left tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/name%3Aleft#values", osm = "http://wiki.openstreetmap.org/wiki/Names#Left_and_right_names")
public interface NameLeftTag
{
    @TagKey(KeyType.LOCALIZED)
    String KEY = "name:left";
}

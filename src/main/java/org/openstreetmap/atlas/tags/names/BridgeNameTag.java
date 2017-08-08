package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagKey.KeyType;

/**
 * OSM bridge:name tag
 *
 * @author alexhsieh
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/search?q=bridge%3Aname", osm = "http://wiki.openstreetmap.org/wiki/Key:bridge:name")
public interface BridgeNameTag
{
    @TagKey(KeyType.LOCALIZED)
    String KEY = "bridge:name";
}

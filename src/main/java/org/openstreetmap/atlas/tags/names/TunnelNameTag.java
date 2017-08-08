package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagKey.KeyType;

/**
 * OSM tunnel:name tag
 *
 * @author alexhsieh
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/search?q=tunnel%3Aname", osm = "http://wiki.openstreetmap.org/wiki/Key:tunnel")
public interface TunnelNameTag
{
    @TagKey(KeyType.LOCALIZED)
    String KEY = "tunnel:name";
}

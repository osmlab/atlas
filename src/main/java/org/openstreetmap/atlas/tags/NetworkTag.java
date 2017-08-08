package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM network tag: While there are some conventions indicated on the OSM wiki, there is a wide
 * variety of possible values based on taginfo so this is subject to validation as any string.
 *
 * @author robert_stack
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/network#values", osm = "http://wiki.openstreetmap.org/wiki/Key:network")
public interface NetworkTag
{
    @TagKey
    String KEY = "network";
}

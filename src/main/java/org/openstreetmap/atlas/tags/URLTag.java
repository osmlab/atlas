package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM url tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.URI, taginfo = "http://taginfo.openstreetmap.org/keys/url#values", osm = "http://wiki.openstreetmap.org/wiki/Key:url")
public interface URLTag
{
    @TagKey
    String KEY = "url";
}

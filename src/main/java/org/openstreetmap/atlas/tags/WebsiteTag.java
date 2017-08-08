package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM website tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.URI, taginfo = "http://taginfo.openstreetmap.org/keys/website#values", osm = "http://wiki.openstreetmap.org/wiki/Key:website")
public interface WebsiteTag
{
    @TagKey
    String KEY = "website";
}

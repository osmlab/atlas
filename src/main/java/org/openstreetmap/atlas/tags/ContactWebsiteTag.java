package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM contact:website tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.URI, taginfo = "http://taginfo.openstreetmap.org/keys/contact%3Awebsite#values", osm = "http://wiki.openstreetmap.org/wiki/Key:contact")
public interface ContactWebsiteTag
{
    @TagKey
    String KEY = "contact:website";
}

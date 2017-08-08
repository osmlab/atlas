package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM contact:fax tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/contact%3Afax#values", osm = "http://wiki.openstreetmap.org/wiki/Key:contact")
public interface ContactFaxTag
{
    @TagKey
    String KEY = "contact:fax";
}

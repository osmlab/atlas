package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM contact:linkedin tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/contact%3Alinkedin#values", osm = "http://wiki.openstreetmap.org/wiki/Key:contact")
public interface ContactLinkedInTag
{
    @TagKey
    String KEY = "contact:linkedin";
}

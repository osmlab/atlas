package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM contact:google_plug tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/contact%3Agoogle_plus#values", osm = "http://wiki.openstreetmap.org/wiki/Key:contact")
public interface ContactGooglePlusTag
{
    @TagKey
    String KEY = "contact:google_plus";
}

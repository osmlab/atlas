package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM contact:twitter tag
 *
 * @author cstaylor
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/contact%3Atwitter#values", osm = "http://wiki.openstreetmap.org/wiki/Key:contact")
public interface ContactTwitterTag
{
    @TagKey
    String KEY = "contact:twitter";
}

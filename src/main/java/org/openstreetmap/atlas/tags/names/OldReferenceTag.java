package org.openstreetmap.atlas.tags.names;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM old_ref tag
 *
 * @author kkonishi2
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/keys/old_ref", osm = "http://wiki.openstreetmap.org/wiki/Key:ref")
public interface OldReferenceTag
{
    @TagKey
    String KEY = "old_ref";
}

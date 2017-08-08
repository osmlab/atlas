package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM destination:ref tag
 *
 * @author alexhsieh
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/keys/destination%3Aref")
public interface DestinationRefTag
{
    @TagKey
    String KEY = "destination:ref";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM destination:ref:to tag
 *
 * @author sbhalekar
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/keys/destination%3Aref%3Ato")
public interface DestinationRefToTag
{
    @TagKey
    String KEY = "destination:ref:to";
}

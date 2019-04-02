package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM destination:int_ref tag
 *
 * @author sbhalekar
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/keys/destination%3Aint_ref")
public interface DestinationIntRefTag
{
    @TagKey
    String KEY = "destination:int_ref";
}

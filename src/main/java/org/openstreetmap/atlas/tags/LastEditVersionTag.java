package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * Annotated class representing the last_edit_version tag in Atlas data. Though a tag in an atlas,
 * this property does not come from a tag in an OSM Entity. It comes from the OSM Entity attribute:
 * version. Every atlas entity will have this tag.
 *
 * @author nhallahan
 */
@Tag(Validation.LONG)
public interface LastEditVersionTag
{
    @TagKey
    String KEY = "last_edit_version";
}

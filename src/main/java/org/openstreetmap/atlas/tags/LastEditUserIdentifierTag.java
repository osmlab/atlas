package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * Annotated class representing the last_edit_user_id tag in Atlas data. Though a tag in an atlas,
 * this property does not come from a tag in an OSM Entity. It comes from the OSM Entity attribute:
 * uid. Every atlas entity will have this tag.
 *
 * @author tony
 */
@Tag(Validation.LONG)
public interface LastEditUserIdentifierTag
{
    @TagKey
    String KEY = "last_edit_user_id";
}

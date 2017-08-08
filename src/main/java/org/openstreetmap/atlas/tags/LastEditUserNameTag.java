package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * Annotated class representing the last_edit_user_name tag in Atlas data. Though a tag in an atlas,
 * this property does not come from a tag in an OSM Entity. It comes from the OSM Entity attribute:
 * user. Every atlas entity will have this tag.
 *
 * @author cstaylor
 */
@Tag(Validation.NON_EMPTY_STRING)
public interface LastEditUserNameTag
{
    @TagKey
    String KEY = "last_edit_user_name";
}

package org.openstreetmap.atlas.tags;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM exit_to:right tag
 *
 * @author alexhsieh
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "https://taginfo.openstreetmap.org/keys/exit_to%3Aright")
public interface ExitToRightTag
{
    @TagKey
    String KEY = "exit_to:right";
}

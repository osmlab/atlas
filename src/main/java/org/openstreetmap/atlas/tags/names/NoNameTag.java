package org.openstreetmap.atlas.tags.names;

import java.util.Optional;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM No Name Tag.
 *
 * @author matthieun
 */
@Tag(value = Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/noname#values", osm = "http://wiki.openstreetmap.org/wiki/Key:noname")
public enum NoNameTag
{
    YES,
    NO;

    @TagKey
    public static final String KEY = "noname";

    public static boolean isNoName(final Taggable taggable)
    {
        final Optional<NoNameTag> noName = Validators.from(NoNameTag.class, taggable);
        return noName.isPresent() && YES == noName.get();
    }
}

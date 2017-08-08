package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM noexit tag
 *
 * @author matthieun
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/noexit#values", osm = "http://wiki.openstreetmap.org/wiki/Key:noexit")
public enum NoExitTag
{
    YES;

    @TagKey
    public static final String KEY = "noexit";

    public static Optional<NoExitTag> get(final Taggable taggable)
    {
        return Validators.from(NoExitTag.class, taggable);
    }
}

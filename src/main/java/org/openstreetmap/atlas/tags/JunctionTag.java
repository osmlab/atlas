package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * OSM junction tag
 *
 * @author cstaylor
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/junction#values", osm = "http://wiki.openstreetmap.org/wiki/Junctions")
public enum JunctionTag
{
    ROUNDABOUT,
    CIRCULAR;

    @TagKey
    public static final String KEY = "junction";

    public static boolean isCircular(final Taggable taggable)
    {
        final Optional<JunctionTag> junction = Validators.from(JunctionTag.class, taggable);
        return junction.isPresent() && CIRCULAR == junction.get();
    }

    public static boolean isRoundabout(final Taggable taggable)
    {
        final Optional<JunctionTag> junction = Validators.from(JunctionTag.class, taggable);
        return junction.isPresent() && ROUNDABOUT == junction.get();
    }
}

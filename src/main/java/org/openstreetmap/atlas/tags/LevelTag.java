package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;

/**
 * OSM level tag
 *
 * @author sayas01
 */
@Tag(value = Tag.Validation.NON_EMPTY_STRING, taginfo = "http://taginfo.openstreetmap.org/keys/level#values", osm = "http://wiki.openstreetmap.org/wiki/Level")
@SuppressWarnings("squid:S1214")
public interface LevelTag
{
    @TagKey
    String KEY = "level";

    static String getTaggedOrImpliedValue(final Taggable taggable, final String impliedValue)
    {
        final Optional<String> taggedValue = getTaggedValue(taggable);
        return taggedValue.isPresent() ? taggedValue.get() : impliedValue;
    }

    static Optional<String> getTaggedValue(final Taggable taggable)
    {
        return taggable.getTag(KEY);
    }
}

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

    /**
     * Checks if two Taggable objects are on the same level or not. As per
     * https://wiki.openstreetmap.org/wiki/Key:level, level=0 is not always at street level and so
     * unlike LayerTag, if the LevelTag is not explicitly given, we cannot imply that the object is
     * at level 0.
     *
     * @param taggableOne
     *            first object to compare
     * @param taggableTwo
     *            second object to compare
     * @return true if object one and object two are on the same level
     */
    static boolean areOnSameLevel(final Taggable taggableOne, final Taggable taggableTwo)
    {
        final Optional<String> levelTagEdgeOne = LevelTag.getTaggedValue(taggableOne);
        final Optional<String> levelTagEdgeTwo = LevelTag.getTaggedValue(taggableTwo);
        if (levelTagEdgeOne.isPresent() && levelTagEdgeTwo.isPresent())
        {
            return levelTagEdgeOne.get().equals(levelTagEdgeTwo.get());
        }
        return !levelTagEdgeOne.isPresent() && !levelTagEdgeTwo.isPresent();
    }

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

package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.LongExtractor;

/**
 * OSM level tag
 *
 * @author sayas01
 */
@Tag(value = Tag.Validation.LONG, range = @Tag.Range(min = -3, max = 5), taginfo = "http://taginfo.openstreetmap.org/keys/level#values", osm = "http://wiki.openstreetmap.org/wiki/Level")

public interface LevelTag
{
    @TagKey
    String KEY = "level";

    static long getMaxLevelValue()
    {
        return LevelTag.class.getDeclaredAnnotation(Tag.class).range().max();
    }

    static long getMinLevelValue()
    {
        return LevelTag.class.getDeclaredAnnotation(Tag.class).range().min();
    }

    static Optional<Long> getTaggedValue(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            final LongExtractor longExtractor = new LongExtractor();
            return longExtractor.validateAndExtract(tagValue.get(),
                    LevelTag.class.getDeclaredAnnotation(Tag.class));
        }
        return Optional.empty();
    }
}

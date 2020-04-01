package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Range;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.OrdinalExtractor;

/**
 * OSM protect_class tag
 *
 * @author bbreithaupt
 */
@Tag(value = Validation.ORDINAL, range = @Range(min = 1, max = 99), taginfo = "https://taginfo.openstreetmap.org/keys/protect_class", osm = "https://wiki.openstreetmap.org/wiki/Key:protect_class")
public interface ProtectClassTag
{
    @TagKey
    String KEY = "protect_class";

    static Optional<Integer> getValue(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            final OrdinalExtractor extractor = new OrdinalExtractor();
            return extractor.validateAndExtract(tagValue.get(),
                    ProtectClassTag.class.getDeclaredAnnotation(Tag.class));
        }

        return Optional.empty();
    }
}

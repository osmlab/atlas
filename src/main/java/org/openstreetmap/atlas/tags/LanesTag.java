package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Range;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.OrdinalExtractor;

/**
 * OSM lanes tag
 *
 * @author robert_stack
 */
@Tag(value = Validation.ORDINAL, range = @Range(min = 1, max = 50), taginfo = "http://taginfo.openstreetmap.org/keys/lanes#values", osm = "http://wiki.openstreetmap.org/wiki/Lanes")
public interface LanesTag
{
    @TagKey
    String KEY = "lanes";

    static Optional<Integer> numberOfLanes(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            final OrdinalExtractor extractor = new OrdinalExtractor();
            return extractor.validateAndExtract(tagValue.get(),
                    LanesTag.class.getDeclaredAnnotation(Tag.class));
        }

        return Optional.empty();
    }
}

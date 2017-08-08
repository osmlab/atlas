package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Range;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.OrdinalExtractor;

/**
 * OSM admin_level tag. Works in conjunction with the {@link BoundaryTag} administrative value.
 *
 * @author matthieun
 */
@Tag(value = Validation.ORDINAL, range = @Range(min = 1, max = 11), taginfo = "https://taginfo.openstreetmap.org/keys/admin_level#values", osm = "http://wiki.openstreetmap.org/wiki/Tag:boundary%3Dadministrative#10_admin_level_values_for_specific_countries")
public interface AdministrativeLevelTag
{
    @TagKey
    String KEY = "admin_level";

    /**
     * Validate the tag and return the Administrative Level tag as an integer between 1 and 11.
     *
     * @param taggable
     *            The object to parse
     * @return the Administrative Level tag as an integer between 1 and 11, if validated.
     */
    static Optional<Integer> getAdministrativeLevel(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            final OrdinalExtractor extractor = new OrdinalExtractor();
            return extractor.validateAndExtract(tagValue.get(),
                    AdministrativeLevelTag.class.getDeclaredAnnotation(Tag.class));
        }

        return Optional.empty();
    }

    static long maximumAdministrativeLevelValue()
    {
        final Tag tag = AdministrativeLevelTag.class.getDeclaredAnnotation(Tag.class);
        final Range range = tag.range();
        return range.max();
    }

    static long minimumAdministrativeLevelValue()
    {
        final Tag tag = AdministrativeLevelTag.class.getDeclaredAnnotation(Tag.class);
        final Range range = tag.range();
        return range.min();
    }
}

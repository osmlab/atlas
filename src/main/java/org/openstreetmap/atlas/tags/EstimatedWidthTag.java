package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Range;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.LengthExtractor;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * OSM estimated width tag
 *
 * @author bbreithaupt
 */
@Tag(value = Validation.DOUBLE, range = @Range(min = 0, max = Integer.MAX_VALUE), taginfo = "https://taginfo.openstreetmap.org/keys/est_width", osm = "https://wiki.openstreetmap.org/wiki/Key:est_width")
public interface EstimatedWidthTag
{
    @TagKey
    String KEY = "est_width";

    static Optional<Distance> get(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            return LengthExtractor.validateAndExtract(tagValue.get());
        }

        return Optional.empty();
    }
}

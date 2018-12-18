package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValue;
import org.openstreetmap.atlas.tags.annotations.TagValue.ValueType;
import org.openstreetmap.atlas.tags.annotations.extraction.LengthExtractor;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * OSM maxwidth tag
 *
 * @author cstaylor
 * @author bbreithaupt
 */
@Tag(taginfo = "http://taginfo.openstreetmap.org/keys/maxwidth#values", osm = "http://wiki.openstreetmap.org/wiki/Key:maxwidth")
public interface MaxWidthTag
{
    @TagKey
    String KEY = "maxwidth";

    @TagValue(ValueType.REGEX)
    String METERS = "(\\d+(\\.\\d+)?|\\.\\d+)(\\sm)?";

    @TagValue(ValueType.REGEX)
    String FEET = "\\d'\\d\"";

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

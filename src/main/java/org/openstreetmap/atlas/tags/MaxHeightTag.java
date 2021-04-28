package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValue;
import org.openstreetmap.atlas.tags.annotations.TagValue.ValueType;
import org.openstreetmap.atlas.tags.annotations.extraction.AltitudeExtractor;

/**
 * OSM maxheight tag: http://taginfo.openstreetmap.org/keys/maxheight#values
 *
 * @author cstaylor
 * @author bbreithaupt
 */
@Tag(value = Validation.DOUBLE, taginfo = "http://taginfo.openstreetmap.org/keys/maxheight#values", osm = "http://wiki.openstreetmap.org/wiki/Key:maxheight")
public interface MaxHeightTag
{
    @TagKey
    String KEY = "maxheight";

    @TagValue
    String DEFAULT = "default";

    @TagValue
    String NONE = "none";

    @TagValue(ValueType.REGEX)
    String METERS = "(\\d+(\\.\\d+)?|\\.\\d+)(\\sm)?";

    @TagValue(ValueType.REGEX)
    String FEET = "\\d'\\d\"";

    static Optional<Altitude> get(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            return AltitudeExtractor.validateAndExtract(tagValue.get());
        }

        return Optional.empty();
    }
}

package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.AltitudeExtractor;

/**
 * OSM height tag: http://taginfo.openstreetmap.org/keys/height#values
 *
 * @author cstaylor
 * @author bbreithaupt
 */
@Tag(value = Validation.LENGTH, taginfo = "http://taginfo.openstreetmap.org/keys/height#values", osm = "http://wiki.openstreetmap.org/wiki/Key:height")
public interface HeightTag
{
    @TagKey
    String KEY = "height";

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

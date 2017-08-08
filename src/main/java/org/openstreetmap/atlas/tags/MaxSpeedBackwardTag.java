package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.TagValue;
import org.openstreetmap.atlas.tags.annotations.extraction.SpeedExtractor;
import org.openstreetmap.atlas.utilities.scalars.Speed;

/**
 * OSM maxspeed:backward tag
 *
 * @author matthieun
 */
@Tag(value = Validation.SPEED, taginfo = "http://taginfo.openstreetmap.org/keys/maxspeed#values", osm = "http://wiki.openstreetmap.org/wiki/Key:maxspeed")
public interface MaxSpeedBackwardTag
{
    @TagKey
    String KEY = "maxspeed:backward";

    @TagValue
    String NONE = "none";

    static Optional<Speed> get(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            return SpeedExtractor.validateAndExtract(tagValue.get());
        }

        return Optional.empty();
    }

    static boolean hasMaxSpeedBackward(final Taggable taggable)
    {
        return taggable.getTag(KEY).isPresent();
    }
}

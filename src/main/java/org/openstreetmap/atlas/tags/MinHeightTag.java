package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.DoubleValidator;

/**
 * OSM min_height tag
 *
 * @author ajayaswal
 */
@Tag(value = Validation.DOUBLE, taginfo = "https://taginfo.openstreetmap.org/keys/min_height#values", osm = "https://wiki.openstreetmap.org/wiki/Key:min_height")
public interface MinHeightTag
{
    @TagKey
    String KEY = "min_height";

    DoubleValidator validator = new DoubleValidator();

    static Optional<Altitude> get(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);

        if (tagValue.isPresent() && validator.isValid(tagValue.get()))
        {
            return Optional.of(Altitude.meters(Double.valueOf(tagValue.get())));
        }

        return Optional.empty();
    }
}

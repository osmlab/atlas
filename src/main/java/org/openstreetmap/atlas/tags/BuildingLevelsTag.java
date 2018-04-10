package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.DoubleValidator;

/**
 * OSM building:levels tag
 *
 * @author robert_stack
 */
@Tag(value = Validation.ORDINAL, taginfo = "http://taginfo.openstreetmap.org/keys/building:levels#values", osm = "http://wiki.openstreetmap.org/wiki/Key:building:levels")
public interface BuildingLevelsTag
{
    @TagKey
    String KEY = "building:levels";

    DoubleValidator validator = new DoubleValidator();

    static Optional<Double> get(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent() && validator.isValid(tagValue.get()))
        {
            return Optional.of(Double.valueOf(tagValue.get()));
        }
        return Optional.empty();
    }
}

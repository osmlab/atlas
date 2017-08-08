package org.openstreetmap.atlas.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.extraction.IsoCountryExtractor;

/**
 * OSM's 3 digit ISO Country tag
 *
 * @author matthieun
 */
@Tag(Validation.ISO3_COUNTRY)
public interface Iso31663CountryTag
{
    @TagKey
    String KEY = "ISO3166-1:alpha3";

    static Iterable<IsoCountry> all(final Taggable taggable)
    {
        final Optional<String> tagValue = taggable.getTag(KEY);
        if (tagValue.isPresent())
        {
            final IsoCountryExtractor extractor = new IsoCountryExtractor();
            final Optional<List<IsoCountry>> countries = extractor.validateAndExtract(
                    tagValue.get(), Iso31663CountryTag.class.getDeclaredAnnotation(Tag.class));
            return countries.isPresent() ? countries.get() : new ArrayList<>();
        }

        return new ArrayList<>();
    }
}

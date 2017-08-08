package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * Extracts ISO2 and ISO3 country code values.
 *
 * @author mgostintsev
 */
public class IsoCountryExtractor implements TagExtractor<List<IsoCountry>>
{
    @Override
    public Optional<List<IsoCountry>> validateAndExtract(final String value, final Tag tag)
    {
        final List<IsoCountry> countries = StringList.split(value, ISOCountryTag.COUNTRY_DELIMITER)
                .stream().map(IsoCountry::forCountryCode).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
        return Optional.of(countries);
    }
}

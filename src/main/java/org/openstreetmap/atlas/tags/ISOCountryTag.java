package org.openstreetmap.atlas.tags;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.common.collect.ImmutableList;

/**
 * Annotated class representing the iso_country_code tag in Atlas data
 *
 * @author cstaylor
 * @author ahsieh
 */
@Tag(Validation.ISO3_COUNTRY)
public interface ISOCountryTag
{
    @TagKey
    String KEY = "iso_country_code";

    String COUNTRY_MISSING = "N/A";
    String COUNTRY_DELIMITER = ",";

    /**
     * Returns all countries from the Taggable's iso_country_code tag.
     *
     * @param taggable
     *            the {@link Taggable} to get the country codes from
     * @return Iterable of all the country codes for this item
     */
    static Iterable<IsoCountry> all(final Taggable taggable)
    {
        final Optional<String> countryCode = taggable.getTag(ISOCountryTag.class, Optional.empty());

        if (countryCode.isPresent())
        {
            final List<String> allIsoCodes = Arrays
                    .asList(countryCode.get().split(COUNTRY_DELIMITER));

            return allIsoCodes.stream().map(isoCode -> IsoCountry.forCountryCode(isoCode))
                    .filter(isoCountry -> isoCountry.isPresent())
                    .map(isoCountry -> isoCountry.get()).collect(Collectors.toList());
        }
        return ImmutableList.of();
    }

    /**
     * Tests if all of the item's iso_country_codes can be found in a list of countries
     *
     * @param countries
     *            A set of countries we want to check in
     * @return Predicate used to test if all of the item's iso_country_codes can be found in the
     *         list of countries
     */
    static Predicate<Taggable> allIn(final IsoCountry... countries)
    {
        if (countries.length == 0)
        {
            return taggable -> false;
        }

        return taggable ->
        {
            final List<IsoCountry> checkCountries = Arrays.asList(countries);

            for (final IsoCountry isoCountry : all(taggable))
            {
                // if any don't match, return false
                if (!checkCountries.contains(isoCountry))
                {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Returns the first country code from the Taggable's iso_country_code tag. BEWARE: This method
     * may be non-deterministic as it depends on the ordering of the country codes
     *
     * @param taggable
     *            The {@link Taggable} to get the first country code from
     * @return The first country code for the item if it exists
     */
    static Optional<IsoCountry> first(final Taggable taggable)
    {
        return Iterables.first(all(taggable));
    }

    /**
     * Tests if any of the item's iso_country_code can be found in a list of countries
     *
     * @param countries
     *            A set of countries we want to check in
     * @return Predicate used to test if any of the item's iso_country_code can be found in the list
     *         of countries
     */
    static Predicate<Taggable> isIn(final IsoCountry... countries)
    {
        if (countries.length == 0)
        {
            return taggable -> false;
        }

        return taggable ->
        {
            final List<IsoCountry> checkCountries = Arrays.asList(countries);

            for (final IsoCountry isoCountry : all(taggable))
            {
                // if any one matches, return true
                if (checkCountries.contains(isoCountry))
                {
                    return true;
                }
            }
            return false;
        };
    }
}

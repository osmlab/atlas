package org.openstreetmap.atlas.tags;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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
 * @author bbreithaupt
 */
@Tag(Validation.ISO3_COUNTRY)
public interface ISOCountryTag
{
    @TagKey
    String KEY = "iso_country_code";

    String COUNTRY_MISSING = "N/A";
    String COUNTRY_DELIMITER = ",";

    static Iterable<String> all(final Taggable taggable)
    {
        final Optional<String> countryCode = taggable.getTag(ISOCountryTag.class, Optional.empty());
        if (countryCode.isPresent())
        {
            return Arrays.asList(countryCode.get().split(COUNTRY_DELIMITER));
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
    static Predicate<Taggable> allIn(final Collection<String> countries)
    {
        if (countries.isEmpty())
        {
            return taggable -> false;
        }

        return taggable ->
        {
            for (final String country : all(taggable))
            {
                // if any don't match, return false
                if (!countries.contains(country))
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
    static Optional<String> first(final Taggable taggable)
    {
        return Iterables.first(all(taggable));
    }

    static Predicate<Taggable> isIn(final Set<String> countries)
    {
        if (countries.isEmpty())
        {
            return taggable -> false;
        }

        return taggable ->
        {
            for (final String country : all(taggable))
            {
                // if any one matches, return true
                if (countries.contains(country))
                {
                    return true;
                }
            }
            return false;
        };
    }

    static Predicate<Taggable> isIn(final String countryToMatch)
    {
        if (countryToMatch == null || countryToMatch.isEmpty())
        {
            return taggable -> false;
        }

        return taggable ->
        {
            for (final String country : all(taggable))
            {
                // if any one matches, return true
                if (countryToMatch.equals(country))
                {
                    return true;
                }
            }
            return false;
        };
    }

    static String join(final Collection<String> countries)
    {
        return String.join(COUNTRY_DELIMITER, countries);
    }
}

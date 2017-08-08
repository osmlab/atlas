package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks if the value of a tag matches one of the supported ISO3 country names in the Java Locale
 * classes
 *
 * @author cstaylor
 */
public class ISO3CountryValidator implements TagValidator
{
    private static Set<String> validISOCountries;

    static
    {
        validISOCountries = Arrays.asList(Locale.getAvailableLocales()).stream()
                .filter(ISO3CountryValidator::hasISO3Country).map(Locale::getISO3Country)
                .collect(Collectors.toSet());
    }

    private static boolean hasISO3Country(final Locale locale)
    {
        try
        {
            locale.getISO3Country();
            return true;
        }
        catch (final MissingResourceException oops)
        {
            return false;
        }
    }

    @Override
    public boolean isValid(final String value)
    {
        return validISOCountries.contains(value);
    }
}

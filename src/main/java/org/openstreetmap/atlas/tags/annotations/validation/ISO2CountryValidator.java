package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Checks if the value of a tag matches one of the supported ISO3 country names in the Java Locale
 * classes
 *
 * @author cstaylor
 */
public class ISO2CountryValidator implements TagValidator
{
    private static Set<String> validISOCountries;

    static
    {
        validISOCountries = Arrays.asList(Locale.getAvailableLocales()).stream()
                .filter(ISO2CountryValidator::hasISO2Country).map(Locale::getCountry)
                .collect(Collectors.toSet());
    }

    private static boolean hasISO2Country(final Locale locale)
    {
        final String country = locale.getCountry();
        return StringUtils.isNotBlank(country) && country.length() == 2;
    }

    @Override
    public boolean isValid(final String value)
    {
        return validISOCountries.contains(value);
    }
}

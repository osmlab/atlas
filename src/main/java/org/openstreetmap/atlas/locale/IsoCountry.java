package org.openstreetmap.atlas.locale;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Java Locale based countries, including ISO2, ISO3, and descriptive country name representations.
 *
 * @author robert_stack
 */
public final class IsoCountry
{
    // Use United States fixed Locale for display use cases
    private static final String LOCALE_LANGUAGE = Locale.ENGLISH.getLanguage();

    private static final int ISO2_LENGTH = 2;
    private static final int ISO3_LENGTH = 3;

    private static Set<String> ALL_COUNTRY_CODES;
    private static final Map<String, String> ISO2_TO_DISPLAY_COUNTRY;
    private static final Map<String, String> ISO2_TO_ISO3;
    private static final Map<String, String> ISO3_TO_ISO2;
    // private static final BiMap<String, String> ISO2_ISO3_MAP;
    private static final Map<String, IsoCountry> ISO_COUNTRIES;

    static
    {
        // All Locale based 2 letter country codes
        final String[] countries = Locale.getISOCountries();

        // Set of language codes--exposed publically through allLanguageCodes()
        ALL_COUNTRY_CODES = Collections.unmodifiableSet(
                Arrays.stream(countries).map(String::intern).collect(Collectors.toSet()));

        // Map from ISO2 to full country name
        ISO2_TO_DISPLAY_COUNTRY = Collections.unmodifiableMap(
                Arrays.stream(countries).collect(Collectors.toMap(iso2 -> iso2.intern(),
                        iso2 -> new Locale(LOCALE_LANGUAGE, iso2).getDisplayCountry().intern())));

        // Map from ISO2 to ISO3
        ISO2_TO_ISO3 = Collections.unmodifiableMap(
                Arrays.stream(countries).collect(Collectors.toMap(iso2 -> iso2.intern(),
                        iso2 -> new Locale(LOCALE_LANGUAGE, iso2).getISO3Country().intern())));

        // Map from ISO3 to ISO2
        // Have verified a 1:1 between ISO2 to ISO3, otherwise there would be overwrites of ISO3
        // keys
        ISO3_TO_ISO2 = Collections
                .unmodifiableMap(ISO2_TO_ISO3.entrySet().stream().collect(Collectors
                        .toMap(iso3 -> iso3.getValue().intern(), iso3 -> iso3.getKey().intern())));

        // TODO Use Guava BiMap
        // ISO2_ISO3_MAP = Collections.unmodifiableMap(Arrays.stream(countries).collect(
        // Collectors.toMap(x -> x.get(0), x -> x.get(1), (a, b) -> b, HashBiMap::create)));

        // Map from ISO2 to IsoCountry
        ISO_COUNTRIES = Collections.unmodifiableMap(Arrays.stream(countries)
                .collect(Collectors.toMap(iso2 -> iso2.intern(), iso2 -> new IsoCountry(iso2))));
    }

    // This validated country code
    private final String iso2CountryCode;
    private final String iso3CountryCode;
    private final String displayCountry;

    /**
     * Provides a set of all Locale based country codes; supports convenience methods that use all
     * country codes
     *
     * @return Set of country codes
     */
    public static Set<String> allCountryCodes()
    {
        return ALL_COUNTRY_CODES;
    }

    /**
     * Provides long name country for country code
     *
     * @param countryCode
     *            2 or 3 character country code, case sensitive (examples "US", "USA")
     * @return Optional of display country string (example "United States")
     */
    public static Optional<String> displayCountry(final String countryCode)
    {
        if (countryCode != null)
        {
            if (countryCode.length() == ISO2_LENGTH && ISO2_TO_ISO3.keySet().contains(countryCode))
            {
                final String displayCountry = ISO2_TO_DISPLAY_COUNTRY.get(countryCode);
                return Optional.ofNullable(displayCountry);
            }
            if (countryCode.length() == ISO3_LENGTH && ISO3_TO_ISO2.keySet().contains(countryCode))
            {
                final String iso2 = ISO3_TO_ISO2.get(countryCode);
                if (iso2 != null)
                {
                    final String displayCountry = ISO2_TO_DISPLAY_COUNTRY.get(iso2);
                    return Optional.ofNullable(displayCountry);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Provides IsoCountry for valid country code
     *
     * @param countryCode
     *            2 or 3 character country code, case sensitive (examples "US", "USA")
     * @return Optional of valid country code, or Optional.empty if not recognized
     */
    public static Optional<IsoCountry> forCountryCode(final String countryCode)
    {
        if (countryCode != null)
        {
            if (countryCode.length() == ISO2_LENGTH && ISO2_TO_ISO3.keySet().contains(countryCode))
            {
                return Optional.ofNullable(ISO_COUNTRIES.get(countryCode));
            }
            else if (countryCode.length() == ISO3_LENGTH
                    && ISO3_TO_ISO2.keySet().contains(countryCode))
            {
                return Optional.ofNullable(ISO_COUNTRIES.get(ISO3_TO_ISO2.get(countryCode)));
            }
        }
        return Optional.empty();
    }

    /**
     * Provides ISO2 string for ISO3
     *
     * @param iso3
     *            3 character country code, case sensitive (example "USA")
     * @return Optional of ISO2 country code, or Optional.empty if not recognized
     */
    public static Optional<String> iso2ForIso3(final String iso3)
    {
        String iso2 = null;
        if (iso3 != null)
        {
            iso2 = ISO3_TO_ISO2.get(iso3);
        }
        return Optional.ofNullable(iso2);
    }

    /**
     * Provides ISO3 string for ISO2
     *
     * @param iso2
     *            2 character country code, case sensitive (example "US")
     * @return Optional of ISO3 country code, or Optional.empty if not recognized
     */
    public static Optional<String> iso3ForIso2(final String iso2)
    {
        String iso3 = null;
        if (iso2 != null)
        {
            iso3 = ISO2_TO_ISO3.get(iso2);
        }
        return Optional.ofNullable(iso3);
    }

    /**
     * Indicates whether the ISO2 or ISO3 country code is valid
     *
     * @param isoCountry
     *            2 or 3 character country code, case sensitive (examples "US", "USA")
     * @return Whether this is a valid ISO2 or ISO3 country code
     */
    public static boolean isValidCountryCode(final String isoCountry)
    {
        if (isoCountry != null)
        {
            if (isoCountry.length() == ISO2_LENGTH && ISO2_TO_ISO3.keySet().contains(isoCountry))
            {
                return true;
            }
            else if (isoCountry.length() == ISO3_LENGTH
                    && ISO3_TO_ISO2.keySet().contains(isoCountry))
            {
                return true;
            }
        }
        return false;
    }

    private IsoCountry(final String iso2)
    {
        this.iso2CountryCode = iso2;
        this.iso3CountryCode = ISO2_TO_ISO3.get(this.iso2CountryCode);
        this.displayCountry = ISO2_TO_DISPLAY_COUNTRY.get(this.iso2CountryCode);
    }

    /**
     * Provides the ISO2 country code for this IsoCountry
     *
     * @return 2 character (ISO2) language code
     */
    public String getCountryCode()
    {
        return this.iso2CountryCode;
    }

    /**
     * Provides the display name for this IsoCountry
     *
     * @return Display country string
     */
    public String getDisplayCountry()
    {
        return this.displayCountry;
    }

    /**
     * Provides the ISO3 country code for this IsoCountry
     *
     * @return 3 character (ISO3) language code
     */
    public String getIso3CountryCode()
    {
        return this.iso3CountryCode;
    }

    @Override
    public String toString()
    {
        return this.getDisplayCountry();
    }
}

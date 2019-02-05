package org.openstreetmap.atlas.locale;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java Locale based countries, including ISO2, ISO3, and descriptive country name representations.
 *
 * @author robert_stack
 * @author lcram
 */
public final class IsoCountry implements Serializable
{
    private static final long serialVersionUID = 8686298246454085812L;

    private static final Logger logger = LoggerFactory.getLogger(IsoCountry.class);

    // Use United States fixed Locale for display use cases
    private static final String LOCALE_LANGUAGE = Locale.ENGLISH.getLanguage();

    private static final int ISO2_LENGTH = 2;
    private static final int ISO3_LENGTH = 3;

    // Package private fields used by other classes in the locale package
    static final Set<String> ALL_COUNTRY_CODES;
    static final Set<String> ALL_DISPLAY_COUNTRIES;
    static final Map<String, String> ISO2_TO_DISPLAY_COUNTRY;
    static final Map<String, String> DISPLAY_COUNTRY_TO_ISO2;
    static final Map<String, String> ISO2_TO_ISO3;
    static final Map<String, String> ISO3_TO_ISO2;
    // private static final BiMap<String, String> ISO2_ISO3_MAP;
    static final Map<String, IsoCountry> ISO_COUNTRIES;

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

        /*
         * Check that country names are actually unique, and log an error if not. NOTE that this
         * relies on English country names. If you are updating this code to handle
         * internationalization, these assumptions may not hold.
         */
        final Map<String, String> countriesSeen = new HashMap<>();
        for (final String iso2Country : countries)
        {
            final String countryName = new Locale(LOCALE_LANGUAGE, iso2Country).getDisplayCountry()
                    .intern();
            if (countriesSeen.containsKey(countryName))
            {
                logger.error("Detected duplicate country name {} -> {} AND {}", countryName,
                        iso2Country, countriesSeen.get(countryName));
            }
            countriesSeen.put(countryName, iso2Country);
        }

        // Map from full country name to ISO2
        DISPLAY_COUNTRY_TO_ISO2 = Collections.unmodifiableMap(Arrays.stream(countries)
                .collect(Collectors.toMap(
                        iso2 -> new Locale(LOCALE_LANGUAGE, iso2).getDisplayCountry().intern(),
                        iso2 -> iso2.intern())));

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

        // Set of display country names, do this one last since it relies on the other maps
        ALL_DISPLAY_COUNTRIES = Collections.unmodifiableSet(ALL_COUNTRY_CODES.stream()
                .map(IsoCountry::displayCountry).map(Optional::get).collect(Collectors.toSet()));
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
     * Provides a set of all Locale based country long names.
     *
     * @return Set of country long names
     */
    public static Set<String> allDisplayCountries()
    {
        return ALL_DISPLAY_COUNTRIES;
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
     * Provides IsoCountry for a valid country display name. Ignores capitalization (e.g. "united
     * stAtes" and "United States" are the same)
     * Provides IsoCountry for a valid country display name.
     *
     * @param displayCountry
     *            the display country name, e.g. "United States"
     * @return an Optional containing the IsoCountry if present
     */
    public static Optional<IsoCountry> forDisplayCountry(final String displayCountry)
    {
        if (displayCountry != null && DISPLAY_COUNTRY_TO_ISO2.containsKey(displayCountry))
        {
            return Optional
                    .ofNullable(ISO_COUNTRIES.get(DISPLAY_COUNTRY_TO_ISO2.get(displayCountry)));
        }
        return Optional.empty();
    }

    /**
     * Provides IsoCountry for a country display name. If the given display name does not perfectly
     * match a valid IsoCountry, this will return the closest string match.
     *
     * @param displayCountry
     *            the display country name, e.g. "united stats"
     * @return an Optional containing the IsoCountry if present
     */
    public static Optional<IsoCountry> forDisplayCountryClosestMatch(final String displayCountry)
    {
        if (displayCountry != null)
        {
            if (DISPLAY_COUNTRY_TO_ISO2.containsKey(displayCountry))
            {
                return Optional
                        .ofNullable(ISO_COUNTRIES.get(DISPLAY_COUNTRY_TO_ISO2.get(displayCountry)));
            }
            else
            {
                final Optional<String> closestCountry = closestIsoCountry(displayCountry);
                if (closestCountry.isPresent())
                {
                    final Optional<IsoCountry> closestMatch = Optional.ofNullable(
                            ISO_COUNTRIES.get(DISPLAY_COUNTRY_TO_ISO2.get(closestCountry.get())));
                    logger.info("Exact match for {} was not found, returning closest match {}",
                            displayCountry, closestMatch);
                    return closestMatch;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Provides IsoCountry for a valid country display name, ignoring case.
     *
     * @param displayCountry
     *            the display country name, e.g. "united states"
     * @return an Optional containing the IsoCountry if present
     */
    public static Optional<IsoCountry> forDisplayCountryIgnoreCase(final String displayCountry)
    {
        if (displayCountry != null)
        {
            /*
             * We want to allow the displayCountry parameter to have inconsistent case. E.g.
             * displayCountry="united States" should match IsoCountry<"United States">
             */
            String foundKey = null;
            for (final String key : DISPLAY_COUNTRY_TO_ISO2.keySet())
            {
                if (displayCountry.equalsIgnoreCase(key))
                {
                    foundKey = key;
                    break;
                }
            }
            return Optional.ofNullable(foundKey).map(DISPLAY_COUNTRY_TO_ISO2::get)
                    .map(ISO_COUNTRIES::get);
        }
        return Optional.empty();
    }

    /**
     * Provides closest IsoCountries for a country display name. If the given display name does not
     * perfectly match a valid IsoCountry, this will return the closest string match up to number of
     * matches.
     *
     * @param number
     *            the number of matches to show
     * @param displayCountry
     *            the display country name, e.g. "united stats"
     * @return an Optional containing the IsoCountry if present
     */
    public static List<IsoCountry> forDisplayCountryTopMatches(final int number,
            final String displayCountry)
    {
        if (displayCountry != null)
        {
            final List<IsoCountry> results = new ArrayList<>();
            if (DISPLAY_COUNTRY_TO_ISO2.containsKey(displayCountry))
            {

                results.add(ISO_COUNTRIES.get(DISPLAY_COUNTRY_TO_ISO2.get(displayCountry)));
            }
            else
            {
                final List<String> closestCountries = closestIsoCountries(number, displayCountry);
                if (!closestCountries.isEmpty())
                {
                    logger.info("Exact match for {} was not found, returning closest {} matches {}",
                            displayCountry, number, closestCountries);
                    results.addAll(closestCountries.stream()
                            .map(countryString -> ISO_COUNTRIES
                                    .get(DISPLAY_COUNTRY_TO_ISO2.get(countryString)))
                            .collect(Collectors.toList()));
                }
            }
            return results;
        }
        return new ArrayList<>();
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

    private static List<String> closestIsoCountries(final int number, final String displayCountry)
    {
        if (number <= 0 || number > ALL_DISPLAY_COUNTRIES.size())
        {
            throw new CoreException(
                    "number " + number + " out of range (0, " + ALL_DISPLAY_COUNTRIES.size() + ")");
        }
        final Map<Integer, String> countryRankings = new HashMap<>();
        for (final String countryName : ALL_DISPLAY_COUNTRIES)
        {
            final int distance = StringUtils.getLevenshteinDistance(displayCountry, countryName);
            countryRankings.put(distance, countryName);
        }
        final List<Entry<Integer, String>> entries = new ArrayList<>(countryRankings.entrySet());
        Collections.sort(entries, (entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()));

        return entries.subList(0, number).stream().map(entry -> entry.getValue())
                .collect(Collectors.toList());
    }

    private static Optional<String> closestIsoCountry(final String displayCountry)
    {
        String closestCountry = null;
        int minimumDistance = Integer.MAX_VALUE;
        for (final String countryName : ALL_DISPLAY_COUNTRIES)
        {
            final int distance = StringUtils.getLevenshteinDistance(displayCountry, countryName);
            if (distance < minimumDistance)
            {
                closestCountry = countryName;
                minimumDistance = distance;
            }
        }

        return Optional.ofNullable(closestCountry);
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

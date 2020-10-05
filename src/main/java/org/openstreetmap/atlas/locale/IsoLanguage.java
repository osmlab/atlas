package org.openstreetmap.atlas.locale;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.openstreetmap.atlas.utilities.collections.EnhancedCollectors;

/**
 * Languages derived from Locale
 *
 * @author robert_stack
 */
public final class IsoLanguage implements Comparable<IsoLanguage>, Serializable
{
    // Use United States fixed Locale for display use cases
    private static final Locale LANGUAGE_LOCALE = Locale.US;

    private static Set<String> ALL_LANGUAGE_CODES;
    private static Set<String> DISPLAY_LANGUAGES_SET;
    private static Map<String, IsoLanguage> ISO_LANGUAGES;

    static
    {
        // All 2 letter language codes
        final String[] languages = Locale.getISOLanguages();

        // Set of language codes--exposed publically through allLanguageCodes()
        ALL_LANGUAGE_CODES = Arrays.stream(languages)
                .collect(EnhancedCollectors.toUnmodifiableSortedSet());

        // Set of display languages; here only to support performant validation of display language.
        // Using a specific fixed Locale rather than system dependent Locale
        DISPLAY_LANGUAGES_SET = Arrays.stream(languages)
                .map(language -> new Locale(language).getDisplayLanguage(LANGUAGE_LOCALE))
                .collect(EnhancedCollectors.toUnmodifiableSortedSet());

        // Map from language codes to IsoLanguages
        ISO_LANGUAGES = Arrays.stream(languages).collect(EnhancedCollectors.toUnmodifiableSortedMap(
                languageCode -> languageCode, languageCode -> new IsoLanguage(languageCode)));
    }

    private final String languageCode;
    private final String displayLanguage;

    /**
     * Convenience method for getting all of the IsoLanguage objects
     *
     * @return a stream of IsoLanguage objects
     */
    public static Stream<IsoLanguage> all()
    {
        return ISO_LANGUAGES.values().stream();
    }

    /**
     * Provides a set of all Locale based language codes; supports convenience methods that use all
     * language codes
     *
     * @return Set of language codes
     */
    public static Set<String> allLanguageCodes()
    {
        return ALL_LANGUAGE_CODES;
    }

    /**
     * Provide the display language per Locale
     *
     * @param languageCode
     *            Locale based language code
     * @return The display language
     */
    public static Optional<String> displayLanguageForLanguageCode(final String languageCode)
    {
        String displayLanguage = null;
        if (languageCode != null)
        {
            final IsoLanguage isoLanguage = ISO_LANGUAGES.get(languageCode);
            if (isoLanguage != null)
            {
                displayLanguage = isoLanguage.getDisplayLanguage();
            }
        }
        return Optional.ofNullable(displayLanguage);
    }

    /**
     * Provides IsoLanguage for valid language code
     *
     * @param languageCode
     *            2 character language code, case sensitive (examples "en", "es")
     * @return Optional of valid language code, or empty if not recognized
     */
    public static Optional<IsoLanguage> forLanguageCode(final String languageCode)
    {
        return Optional.ofNullable(ISO_LANGUAGES.get(languageCode));
    }

    /**
     * Check if display language is valid
     *
     * @param displayLanguage
     *            Display language (example "United States")
     * @return whether this display language is valid
     */
    public static boolean isValidDisplayLanguage(final String displayLanguage)
    {
        return displayLanguage != null && DISPLAY_LANGUAGES_SET.contains(displayLanguage);
    }

    /**
     * Check if language code is valid
     *
     * @param languageCode
     *            2 letter language code
     * @return whether this language code is valid
     */
    public static boolean isValidLanguageCode(final String languageCode)
    {
        return languageCode != null && ISO_LANGUAGES.containsKey(languageCode);
    }

    private IsoLanguage(final String languageCode)
    {
        this.languageCode = languageCode;
        // Using a specific fixed Locale rather than system dependent Locale
        this.displayLanguage = new Locale(languageCode).getDisplayLanguage(LANGUAGE_LOCALE);
    }

    @Override
    public int compareTo(final IsoLanguage other)
    {
        if (this == other)
        {
            return 0;
        }
        return this.languageCode.compareTo(other.getLanguageCode());
    }

    @Override
    public boolean equals(final Object other)
    {
        return other instanceof IsoLanguage && this.compareTo((IsoLanguage) other) == 0;
    }

    /**
     * Provides the display language for this IsoLanguage
     *
     * @return Display language string
     */
    public String getDisplayLanguage()
    {
        return this.displayLanguage;
    }

    /**
     * Provides the language code for this IsoLanguage
     *
     * @return 2 character language code
     */
    public String getLanguageCode()
    {
        return this.languageCode;
    }

    @Override
    public int hashCode()
    {
        return this.getLanguageCode().hashCode();
    }
}

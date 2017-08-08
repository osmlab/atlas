package org.openstreetmap.atlas.tags.names;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.Taggable.TagSearchOption;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

import com.google.common.collect.ImmutableMap;

/**
 * When we need results across multiple languages at the same time this hides a lot of the
 * boilerplate code that would be required if we used the NameFinder class directly.
 *
 * @author ihillberg
 * @author cstaylor
 */
public class BulkNameFinder implements Serializable
{

    /**
     * BulkFind's findIn method will return an implementation of this interface.
     *
     * @author cstaylor
     */
    public interface BulkFindResults
    {
        /**
         * Returns an optional map of name/value pairs for the requested name tags in the supplied
         * language. If the language was not requested during the initial search, the Optional will
         * be empty.
         *
         * @param language
         *            we want the tags for this language only
         * @return a map of the name/value pairs for the name tags
         */
        Optional<Map<Class<?>, String>> allValuesFor(Optional<IsoLanguage> language);

        /**
         * This method will create a map of basekeyname:language to value. This is good for bulk
         * editing a set all at once. Note: The keys have been transformed into strings
         *
         * @return the flattened list of name/value pairs
         */
        Map<String, String> flatten();

        /**
         * Returns an iterable containing all of the languages encountered when creating these
         * results
         *
         * @return an iterable list of IsoLanguages
         */
        Iterable<IsoLanguage> languagesFound();

        /**
         * Returns a single value for the language and name tag. If the request didn't find a tag by
         * the type, Optional.empty() will be returned.
         *
         * @param language
         *            we want the tag value for this language
         * @param tagClass
         *            the name tag we want
         * @return an optional containing the value if it exists or empty if it doesn't
         */
        Optional<String> valueFor(Optional<IsoLanguage> language, Class<?> tagClass);
    }

    /**
     * Internal implementation of the BulkFindResults interface.
     *
     * @author cstaylor
     */
    private static final class DefaultBulkFindResults implements BulkFindResults
    {
        private final Map<IsoLanguage, Map<Class<?>, String>> localizedResults = new HashMap<>();

        private final Map<Class<?>, String> nonLocalizedResults = new LinkedHashMap<>();

        private Map<String, String> flattenedMap = new HashMap<>();

        @Override
        public Optional<Map<Class<?>, String>> allValuesFor(final Optional<IsoLanguage> language)
        {
            return Optional.ofNullable(language.isPresent()
                    ? this.localizedResults.get(language.get()) : this.nonLocalizedResults);
        }

        @Override
        public Map<String, String> flatten()
        {
            return this.flattenedMap;
        }

        @Override
        public Iterable<IsoLanguage> languagesFound()
        {
            return this.localizedResults.keySet();
        }

        @Override
        public Optional<String> valueFor(final Optional<IsoLanguage> language,
                final Class<?> tagClass)
        {
            final Map<Class<?>, String> results = language.isPresent()
                    ? this.localizedResults.get(language.get()) : this.nonLocalizedResults;

            return Optional.ofNullable(results == null ? null : results.get(tagClass));
        }

        private void completed()
        {
            final Map<String, String> temporaryMap = new HashMap<>();
            for (final Entry<IsoLanguage, Map<Class<?>, String>> entry : this.localizedResults
                    .entrySet())
            {
                final Optional<IsoLanguage> currentLanguage = Optional.of(entry.getKey());
                for (final Entry<Class<?>, String> itemEntry : entry.getValue().entrySet())
                {
                    Validators.localizeKeyName(itemEntry.getKey(), currentLanguage)
                            .ifPresent(localizedKeyName ->
                            {
                                temporaryMap.put(localizedKeyName, itemEntry.getValue());
                            });
                }
            }

            for (final Entry<Class<?>, String> entry : this.nonLocalizedResults.entrySet())
            {
                Validators.localizeKeyName(entry.getKey(), Optional.empty())
                        .ifPresent(nonLocalizedKeyName ->
                        {
                            temporaryMap.put(nonLocalizedKeyName, entry.getValue());
                        });
            }
            this.flattenedMap = new ImmutableMap.Builder<String, String>().putAll(temporaryMap)
                    .build();
        }

        private void put(final IsoLanguage language, final Class<?> tag, final String value)
        {
            Map<Class<?>, String> mapping = this.localizedResults.get(language);
            if (mapping == null)
            {
                mapping = new HashMap<>();
                this.localizedResults.put(language, mapping);
            }
            mapping.put(tag, value);
        }

        private void put(final IsoLanguage language, final Map<Class<?>, String> results)
        {
            this.localizedResults.put(language, results);
        }

        private void put(final Map<Class<?>, String> results)
        {
            this.nonLocalizedResults.putAll(results);
        }
    }

    private static final long serialVersionUID = -7709121230794406053L;

    private final LinkedHashSet<IsoLanguage> requestedLanguages = new LinkedHashSet<>();

    private final NameFinder finder = new NameFinder();

    private boolean forceLocalized;

    /**
     * Convenience method that configures the underlying NameFinder for its standard set of tags to
     * search
     *
     * @return fluent interface returns this
     */
    public static BulkNameFinder createStandardSet()
    {
        final BulkNameFinder returnValue = new BulkNameFinder();
        returnValue.finder.withTags(NameFinder.STANDARD_TAGS);
        return returnValue;
    }

    /**
     * Convenience method that adds all languages core is configured to use
     *
     * @return fluent interface returns this
     */
    public BulkNameFinder allLanguages()
    {
        this.requestedLanguages.addAll(IsoLanguage.allLanguageCodes().stream()
                .map(languageCode -> IsoLanguage.forLanguageCode(languageCode).get())
                .collect(Collectors.toSet()));

        return this;
    }

    /**
     * Based on the current settings of BulkNameFinder, search taggable for the localized values of
     * the keys in question and return a BulkFindResults with the resulting tags and values.
     *
     * @param taggable
     *            what we're searching for
     * @return the results of the search
     */
    public BulkFindResults findIn(final Taggable taggable)
    {
        /*
         * We don't want the name finder to outsmart us and pull out the non-localized value if the
         * localized one doesn't exist
         */
        if (this.forceLocalized)
        {
            this.finder.forceLocalized();
        }
        else
        {
            this.finder.localizedOnly();
        }
        final DefaultBulkFindResults results = new DefaultBulkFindResults();
        for (final IsoLanguage language : this.requestedLanguages)
        {
            results.put(language, this.finder.inLanguage(language).all(taggable));
        }
        /*
         * And the non-localized values
         */
        results.put(this.finder.inLanguage(null).all(taggable));
        /*
         * When we call completed, DefaultBulkFindResults will run through all of the data and build
         * an immutable flattened map of the name/value pairs so calls to flatten will return the
         * same map object. This is just an optimization so we don't recalculate the same results on
         * multiple calls to flatten on the same BulkFindResults object
         */
        results.completed();
        return results;
    }

    /**
     * Based on the current tag class settings of BulkNameFinder, search taggable for the localized
     * values of the keys in question using the language of the values present and return a
     * BulkFindResults with the resulting tags and values.
     *
     * @param taggable
     *            what we're searching for
     * @param searchOptions
     *            optional list of flags that alter the behavior of the underlying tag value search
     * @return the results of the search
     */
    public BulkFindResults findInWithMyLanguages(final Taggable taggable,
            final TagSearchOption... searchOptions)
    {
        final EnumSet<TagSearchOption> searchOptionSet = searchOptions.length > 0
                ? EnumSet.copyOf(Arrays.asList(searchOptions))
                : EnumSet.noneOf(TagSearchOption.class);
        final TagSearchOption localizationOption = searchOptionSet
                .contains(TagSearchOption.FORCE_ALL_LOCALIZED_ONLY)
                        ? TagSearchOption.FORCE_ALL_LOCALIZED_ONLY : TagSearchOption.LOCALIZED_ONLY;

        /*
         * We don't want the name finder to outsmart us and pull out the non-localized value if the
         * localized one doesn't exist
         */
        final DefaultBulkFindResults results = new DefaultBulkFindResults();

        for (final Class<?> currentTag : this.finder.getTagNames())
        {
            if (Validators.hasLocalizedTagKey(currentTag)
                    || localizationOption == TagSearchOption.FORCE_ALL_LOCALIZED_ONLY)
            {
                taggable.languagesFor(currentTag, searchOptions).ifPresent(languages ->
                {
                    for (final IsoLanguage language : languages)
                    {
                        taggable.getTag(currentTag, Optional.of(language), localizationOption)
                                .ifPresent(value ->
                                {
                                    results.put(language, currentTag, value);
                                });
                    }
                });
            }
        }

        /*
         * And the non-localized values
         */
        results.put(this.finder.inLanguage(null).all(taggable));
        /*
         * When we call completed, DefaultBulkFindResults will run through all of the data and build
         * an immutable flattened map of the name/value pairs so calls to flatten will return the
         * same map object. This is just an optimization so we don't recalculate the same results on
         * multiple calls to flatten on the same BulkFindResults object
         */
        results.completed();
        return results;
    }

    public BulkNameFinder forceLocalized()
    {
        this.forceLocalized = true;
        return this;
    }

    /**
     * Which language of localized tags are we looking for in the name finder?
     *
     * @param languages
     *            we'll want the values of localizable tags in these languages
     * @return fluent interface returns this
     */
    public BulkNameFinder withLanguage(final IsoLanguage... languages)
    {
        this.requestedLanguages.addAll(Arrays.asList(languages));
        return this;
    }

    /**
     * Which tags are we looking for in the underlying name finder?
     *
     * @param tags
     *            the list of tags we'd like to search entities for
     * @return fluent interface returns this
     */
    public BulkNameFinder withTags(final Class<?>... tags)
    {
        this.finder.withTags(tags);
        return this;
    }
}

package org.openstreetmap.atlas.tags;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

/**
 * Any class that should expose OSM tags can implement this interface for more complex interactions
 * with tag validation.
 *
 * @author cstaylor
 * @author matthieun
 */
public interface Taggable
{
    /**
     * Options for the localized tag search:
     * <ul>
     * <li>DEFAULT - return the non-localized value of a tag if the localized value for a given
     * language is not found</li>
     * <li>LOCALIZED_ONLY - only return the localized value if the tag is a localized tag for a
     * given language; empty otherwise</li>
     * <li>FORCE_ALL_LOCALIZED_ONLY - same as LOCALIZED_ONLY but treat non-localized tags as
     * localizable</li>
     * </ul>
     *
     * @author cstaylor
     */
    enum TagSearchOption
    {
        DEFAULT,
        LOCALIZED_ONLY,
        FORCE_ALL_LOCALIZED_ONLY
    }

    static Taggable with(final Collection<Tag> tagCollection)
    {
        final Map<String, String> tags = new HashMap<>();
        tagCollection.forEach(tag -> tags.put(tag.getKey(), tag.getValue()));
        return with(tags);
    }

    static Taggable with(final Map<String, String> tags)
    {
        return new Taggable()
        {
            @Override
            public Optional<String> getTag(final String key)
            {
                return Optional.ofNullable(tags.get(key));
            }

            @Override
            public String toString()
            {
                return tags.toString();
            }
        };
    }

    static Taggable with(final String... tags)
    {
        return with(Maps.hashMap(tags));
    }

    /**
     * Utility function to test if an entity's tag value starts with some given values.
     *
     * @param key
     *            The tag key
     * @param matches
     *            The matching values
     * @return True if the tag's value matches at least one of the matching values.
     */
    default boolean containsValue(final String key, final Iterable<String> matches)
    {
        final Optional<String> valueOption = getTag(key);
        if (valueOption.isPresent())
        {
            final String value = valueOption.get();
            for (final String candidate : matches)
            {
                if (candidate.startsWith("!"))
                {
                    if (candidate.length() > 1)
                    {
                        final String forbiddenValue = candidate.substring(1);
                        if (!value.equalsIgnoreCase(forbiddenValue))
                        {
                            return true;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
                if ("*".equals(candidate) || value.equalsIgnoreCase(candidate))
                {
                    return true;
                }
                if (candidate != null && candidate.startsWith("*")
                        && value.endsWith(candidate.substring(1)))
                {
                    return true;
                }
                if (candidate != null && candidate.endsWith("*")
                        && value.startsWith(candidate.substring(0, candidate.length() - 1)))
                {
                    return true;
                }
            }
        }
        else
        {
            for (final String candidate : matches)
            {
                if (!candidate.startsWith("!"))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Neat single place where we can get tags by their language if they exist, otherwise return the
     * default without the localized name
     *
     * @param key
     *            the base key we want to find
     * @param language
     *            the optional language's version of the tag we want to find
     * @param searchOptions
     *            search options for finding the value of a key. Sometimes we only want to check the
     *            localized level and not bring in the non-localized value
     * @return the value of the localized version of the tag if it exists, the value of the base key
     *         if the localized one is missing, or an empty optional if neither are available
     */
    default Optional<String> getTag(final Class<?> key, final Optional<IsoLanguage> language,
            final TagSearchOption... searchOptions)
    {
        final EnumSet<TagSearchOption> searchOptionSet = searchOptions.length > 0
                ? EnumSet.copyOf(Arrays.asList(searchOptions))
                : EnumSet.noneOf(TagSearchOption.class);

        final Optional<String> localizedKeyName = Validators.localizeKeyName(key, language,
                searchOptions);
        if (localizedKeyName.isPresent())
        {
            final Optional<String> localizedValue = getTag(localizedKeyName.get());
            if (localizedValue.isPresent()
                    || searchOptionSet.contains(TagSearchOption.LOCALIZED_ONLY)
                    || searchOptionSet.contains(TagSearchOption.FORCE_ALL_LOCALIZED_ONLY))
            {
                return localizedValue;
            }
            final Optional<String> optionalKey = Validators.localizeKeyName(key, Optional.empty());
            if (optionalKey.isPresent())
            {
                return getTag(optionalKey.get());
            }
        }
        return Optional.empty();
    }

    Optional<String> getTag(String key);

    /**
     * Some taggables support fetching all keys, some don't
     *
     * @return all of the tag keys and their values
     */
    default Map<String, String> getTags()
    {
        return new HashMap<>();
    }

    /**
     * Will retrieve tags based on a filter
     *
     * @param filter
     *            The predicate to test each tag by
     * @return The map of filtered tags.
     */
    default Map<String, String> getTags(final Predicate<String> filter)
    {
        return this.getTags().entrySet().stream().filter(item -> filter.test(item.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * @param tags
     *            A tag map to compare to
     * @return True if this contains at least one tag specified in the tag map
     */
    default boolean hasAtLeastOneOf(final Map<String, String> tags)
    {
        for (final Map.Entry<String, String> entry : tags.entrySet())
        {
            final String key = entry.getKey();
            final String value = entry.getValue();
            final Optional<String> myValue = getTag(key);

            if (myValue.isPresent())
            {
                if ("*".equals(value))
                {
                    return true;
                }
                if (value.equals(myValue.get()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return the set of languages explicitly set on the tag provided.
     *
     * @param tag
     *            check this tag on this Taggable for explicitly defined languages
     * @param searchOptions
     *            optional list of flags that alter the behavior of the underlying tag value search
     * @return the optional set of explicitly listed languages found
     */
    default Optional<Set<IsoLanguage>> languagesFor(final Class<?> tag,
            final TagSearchOption... searchOptions)
    {
        final TreeSet<IsoLanguage> returnValue = new TreeSet<>();

        final EnumSet<TagSearchOption> searchOptionSet = searchOptions.length > 0
                ? EnumSet.copyOf(Arrays.asList(searchOptions))
                : EnumSet.noneOf(TagSearchOption.class);

        if (!searchOptionSet.contains(TagSearchOption.FORCE_ALL_LOCALIZED_ONLY)
                && !Validators.hasLocalizedTagKey(tag))
        {
            throw new CoreException("{} isn't a localizable tag", tag.getName());
        }

        final String prefix = Validators.TagKeySearch.findTagKeyIn(tag)
                .orElseThrow(
                        () -> new CoreException("Could not find key for tag {}", tag.getName()))
                .getKeyName();
        final int prefixLength = prefix.length();
        for (final String key : this.getTags().keySet())
        {
            if (key.startsWith(prefix) && key.length() > prefixLength)
            {
                final LocalizedTagNameWithOptionalDate parser = new LocalizedTagNameWithOptionalDate(
                        key);
                parser.getLanguage().ifPresent(returnValue::add);
            }
        }

        return Optional.of(returnValue);
    }

    /**
     * Get the value for this tag key
     *
     * @param key
     *            The tag key
     * @return The value. null if the value does not exist
     */
    default String tag(final String key)
    {
        return getTag(key).orElse(null);
    }
}

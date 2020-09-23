package org.openstreetmap.atlas.tags.filters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.tags.Taggable;

/**
 * This {@link Taggable} filter relies on regex patterns to verify specific tag values.
 * 
 * @author mm-ciub on 11/09/2020.
 */
public class RegexTaggableFilter implements Predicate<Taggable>, Serializable
{
    private final Set<String> tagNames;
    private final Set<Pattern> regexPatterns;
    private final Map<String, Set<String>> exceptions;

    /**
     * @param tagNames
     *            - the set of tag names who's value will be tested
     * @param regex
     *            - a set of regex strings that will validate the value for each tag
     * @param exceptions
     *            - a map of tag names and values that are valid regardless if they match the regex
     *            pattern
     */
    public RegexTaggableFilter(final Set<String> tagNames, final Set<String> regex,
            final Map<String, Set<String>> exceptions)
    {
        this.tagNames = tagNames;
        this.regexPatterns = regex.stream().map(Pattern::compile).collect(Collectors.toSet());
        this.exceptions = exceptions != null ? exceptions : new HashMap<>();
    }

    /**
     * Returns a joined String containing the names of the tags that match at least one of the regex
     * patterns and are not an exception
     * 
     * @param taggable
     *            - the element containing the tags to be checked
     * @return String - example: source,barrier,boundary
     */
    public String getMatchedTags(final Taggable taggable)
    {
        final Set<String> matchedTags = findMatches(taggable);
        return String.join(",", matchedTags);
    }

    @Override
    public boolean test(final Taggable taggable)
    {
        final Set<String> matchedTags = findMatches(taggable);
        return !matchedTags.isEmpty();
    }

    private Set<String> findMatches(final Taggable taggable)
    {
        final Set<String> matchedTags = new HashSet<>();
        for (final String tagName : this.tagNames)
        {
            final Optional<String> tagValue = taggable.getTag(tagName);
            if (tagValue.isPresent())
            {
                final Optional<Matcher> match = this.regexPatterns.stream()
                        .map(pattern -> pattern.matcher(tagValue.get())).filter(Matcher::find)
                        .findAny();
                if (match.isPresent() && !(this.exceptions.containsKey(tagName)
                        && this.exceptions.get(tagName).contains(tagValue.get())))
                {
                    matchedTags.add(tagName);
                }
            }
        }
        return matchedTags;
    }
}

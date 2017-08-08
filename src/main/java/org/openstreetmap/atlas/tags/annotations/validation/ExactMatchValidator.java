package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks if the value of the tag matches a list of known good values
 *
 * @author cstaylor
 */
public class ExactMatchValidator implements TagValidator
{
    private final Set<String> values;
    private final Set<Pattern> regexes;

    public ExactMatchValidator()
    {
        this.values = new HashSet<>();
        this.regexes = new HashSet<>();
    }

    @Override
    public boolean isValid(final String value)
    {
        if (this.values.contains(value))
        {
            return true;
        }

        for (final Pattern pattern : this.regexes)
        {
            if (pattern.matcher(value).matches())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Add regular expression patterns to the validator
     *
     * @param regexes
     *            regular expression encoded in a String
     * @return fluent interface means we return this
     */
    public ExactMatchValidator withRegularExpressions(final String... regexes)
    {
        this.regexes.addAll(Arrays.asList(regexes).stream().map(regex -> Pattern.compile(regex))
                .collect(Collectors.toSet()));
        return this;
    }

    /**
     * Add exact strings to the validator
     *
     * @param values
     *            exact values we want to match
     * @return fluent interface means we return this
     */
    public ExactMatchValidator withValues(final String... values)
    {
        this.values.addAll(Arrays.asList(values));
        return this;
    }

}

package org.openstreetmap.atlas.tags.filters;

import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.Converter;

/**
 * @author lcram
 */
public class TaggableFilterToMatcherConverter implements Converter<TaggableFilter, TaggableMatcher>
{
    protected static String toTaggableMatcherDefinition(final TaggableFilter filter)
    {
        if (filter.getSimple() != null)
        {
            final String definition = filter.getDefinition().orElseThrow(
                    () -> new CoreException("Simple filter definition was not present"));
            final String[] split = definition.split("->");
            if (split.length != 2)
            {
                throw new CoreException("Array length was not 2 for split('->') on definition `{}'",
                        definition);
            }
            final String key = split[0];
            final String value = split[1];

            final String[] commaSeparatedValues = value.split(",");
            if (commaSeparatedValues.length == 1)
            {
                return getKeyValueForSingleValue(key, value);
            }
            for (final String commaSeparatedValue : commaSeparatedValues)
            {
                // TODO fix
                return getKeyValueForMultiValue(key, commaSeparatedValue);
            }
        }
        return "(" + new StringList(filter.getChildren().stream()
                .map(TaggableFilterToMatcherConverter::toTaggableMatcherDefinition)
                .collect(Collectors.toList())).join(" " + filter.getTreeBoolean().separator() + " ")
                + ")";
    }

    private static String getKeyValueForMultiValue(final String key,
            final String commaSeparatedValue)
    {
        // TODO fix
        return key + "=" + commaSeparatedValue;
    }

    private static String getKeyValueForSingleValue(final String key, final String value)
    {
        if (value.charAt(0) == '!' && value.length() == 1)
        {
            // case foo->!
            return "!" + key;
        }
        else if (value.charAt(0) == '!' && value.length() > 1)
        {
            // case foo->!bar
            final String valueWithNoLeadingBang = value.substring(1);
            return "(" + key + "!=" + valueWithNoLeadingBang + " | " + "!" + key + ")";
        }
        else if (value.charAt(0) == '*' && value.length() == 1)
        {
            // case foo->*
            return key;
        }
        else if (value.charAt(0) == '*' && value.length() > 1)
        {
            // case foo->*bar
            return key + "=" + "/.*" + value + "/";
        }
        else if (value.charAt(value.length() - 1) == '*' && value.length() > 1)
        {
            // case foo->bar*
            return key + "=" + "/.*" + value + "/";
        }
        else
        {
            // case foo->bar
            return key + "=" + value;
        }
    }

    @Override
    public TaggableMatcher convert(final TaggableFilter filter)
    {
        return TaggableMatcher.from(toTaggableMatcherDefinition(filter));
    }
}

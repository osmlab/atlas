package org.openstreetmap.atlas.tags.filters;

import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.tags.filters.matcher.parsing.Token;
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
            return getKeyValueForMultiValue(key, commaSeparatedValues);
        }
        return Token.TokenType.PAREN_OPEN.getLiteralValue()
                + new StringList(filter.getChildren().stream()
                        .map(TaggableFilterToMatcherConverter::toTaggableMatcherDefinition)
                        .collect(Collectors.toList()))
                                .join(" " + filter.getTreeBoolean().separator() + " ")
                + Token.TokenType.PAREN_CLOSE.getLiteralValue();
    }

    private static String getKeyValueForMultiValue(final String key,
            final String[] commaSeparatedValues)
    {
        final StringBuilder keyValue = new StringBuilder();
        keyValue.append(key);
        keyValue.append(Token.TokenType.EQUAL.getLiteralValue());
        keyValue.append(Token.TokenType.PAREN_OPEN.getLiteralValue());

        // check for illegal values
        final String commaSeparatedValuesString = new StringList(commaSeparatedValues).join(",");
        for (final String value : commaSeparatedValues)
        {
            if (Token.TokenType.BANG.getLiteralValue().equals(value))
            {
                throw new CoreException(
                        "Cannot transpile `{}->{}' since composite value `{}' contains a lone `{}' operator."
                                + "\n"
                                + "expression `{}->{}' is ambiguous and order dependent, please rewrite your TaggableFilter to remove it.",
                        key, commaSeparatedValuesString, commaSeparatedValuesString,
                        Token.TokenType.BANG.getLiteralValue(), key, commaSeparatedValuesString);
            }
            if ("*".equals(value))
            {
                throw new CoreException(
                        "Cannot transpile `{}->{}' since composite value `{}' contains a lone `*' operator."
                                + "\n"
                                + "expression `{}->{}' is ambiguous and order dependent, please rewrite your TaggableFilter to remove it.",
                        key, commaSeparatedValuesString, commaSeparatedValuesString, key,
                        commaSeparatedValuesString);
            }
        }

        keyValue.append(new StringList(commaSeparatedValues)
                .join(" " + Token.TokenType.OR.getLiteralValue() + " "));
        keyValue.append(Token.TokenType.PAREN_CLOSE.getLiteralValue());
        return keyValue.toString();

    }

    private static String getKeyValueForSingleValue(final String key, final String value)
    {
        if (value.charAt(0) == Token.TokenType.BANG.getLiteralValue().charAt(0)
                && value.length() == 1)
        {
            // case foo->!
            // return !foo
            return Token.TokenType.BANG.getLiteralValue() + key;
        }
        else if (value.charAt(0) == Token.TokenType.BANG.getLiteralValue().charAt(0)
                && value.length() > 1)
        {
            // case foo->!bar
            final String valueWithNoLeadingBang = value.substring(1);
            // return (foo!=bar | !foo)
            return Token.TokenType.PAREN_OPEN.getLiteralValue() + key
                    + Token.TokenType.BANG_EQUAL.getLiteralValue() + valueWithNoLeadingBang + " "
                    + Token.TokenType.OR.getLiteralValue() + " "
                    + Token.TokenType.BANG.getLiteralValue() + key
                    + Token.TokenType.PAREN_CLOSE.getLiteralValue();
        }
        else if (value.charAt(0) == '*' && value.length() == 1)
        {
            // case foo->*
            // return foo
            return key;
        }
        else if (value.charAt(0) == '*' && value.length() > 1)
        {
            // case foo->*bar
            final String newValue = value.substring(1);
            if (hasRegexCharacter(newValue))
            {
                throw new CoreException(
                        "Cannot transpile `{}->{}' since new value `{}' contains a regex control character.",
                        key, value, newValue);
            }
            // return foo=/.*bar/
            return key + Token.TokenType.EQUAL.getLiteralValue()
                    + Token.TokenType.REGEX.getLiteralValue() + ".*" + newValue
                    + Token.TokenType.REGEX.getLiteralValue();
        }
        else if (value.charAt(value.length() - 1) == '*' && value.length() > 1)
        {
            // case foo->bar*
            final String newValue = value.substring(0, value.length() - 1);
            if (hasRegexCharacter(newValue))
            {
                throw new CoreException(
                        "Cannot transpile `{}->{}' since new value `{}' contains a regex control character.",
                        key, value, newValue);
            }
            // return foo=/bar.*/
            return key + Token.TokenType.EQUAL.getLiteralValue()
                    + Token.TokenType.REGEX.getLiteralValue() + newValue + ".*"
                    + Token.TokenType.REGEX.getLiteralValue();
        }
        else
        {
            // case foo->bar
            // return foo=bar
            return key + Token.TokenType.EQUAL.getLiteralValue() + value;
        }
    }

    private static boolean hasRegexCharacter(final String string)
    {
        return string.matches("^.*[^a-zA-Z0-9_ ].*$");
    }

    @Override
    public TaggableMatcher convert(final TaggableFilter filter)
    {
        String taggableMatcherDefinition = toTaggableMatcherDefinition(filter);
        /*
         * Remove leading `(' and trailing `)' if present, since they are redundant. For compound
         * expressions, the converter always adds a redundant pair of parentheses (due to the
         * implementation), so we know it is safe to remove them. There may be additional redundant
         * parentheses that we miss. For example, `foo->bar&baz->bat' will become `((foo=bar &
         * baz=bat))', so we'll remove the first pair of redundant parentheses but miss the inner
         * pair. Ultimately we should find a better way to handle this.
         */
        if (taggableMatcherDefinition.charAt(0) == Token.TokenType.PAREN_OPEN.getLiteralValue()
                .charAt(0)
                && taggableMatcherDefinition.charAt(taggableMatcherDefinition.length()
                        - 1) == Token.TokenType.PAREN_CLOSE.getLiteralValue().charAt(0))
        {
            taggableMatcherDefinition = taggableMatcherDefinition.substring(1,
                    taggableMatcherDefinition.length() - 1);
        }
        return TaggableMatcher.from(taggableMatcherDefinition);
    }
}

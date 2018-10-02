package org.openstreetmap.atlas.tags.filters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.TaggableFilter.TreeBoolean;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Parses a {@link TaggableFilter}'s line.
 *
 * @author matthieun
 */
public class LineFilterConverter implements TwoWayConverter<String, TaggableFilter>
{
    private static final String VALUES_SEPARATOR = ",";
    private static final String KEY_VALUE_SEPARATOR = "->";

    @Override
    public String backwardConvert(final TaggableFilter object)
    {
        return backwardConvert(object, 0);
    }

    @Override
    public TaggableFilter convert(final String object)
    {
        return convert(object, TreeBoolean.OR, 0);
    }

    private String backwardConvert(final TaggableFilter object, final int numberOfOccurrences)
    {
        final Optional<String> definition = object.getDefinition();
        if (definition.isPresent())
        {
            return definition.get();
        }
        else
        {
            final int numberOfSeparators = numberOfSeparators(numberOfOccurrences);
            final String separatorCharacter = object.getTreeBoolean().separator();
            final StringList separatorList = new StringList();
            for (int i = 0; i < numberOfSeparators; i++)
            {
                separatorList.add(separatorCharacter);
            }
            final String separator = separatorList.join("");
            final StringList result = new StringList();
            object.getChildren()
                    .forEach(child -> result.add(backwardConvert(child, numberOfOccurrences + 1)));
            return result.join(separator);
        }
    }

    private TaggableFilter convert(final String object, final TreeBoolean treeBoolean,
            final int numberOfOccurrences)
    {
        final String regex = regex(treeBoolean.separator(),
                numberOfSeparators(numberOfOccurrences));
        final StringList split = StringList.splitByRegex(object, regex);
        final List<TaggableFilter> children = new ArrayList<>();
        if (split.size() > 1)
        {
            // There is a split on the initial operation
            for (final String value : split)
            {
                children.add(convert(value, treeBoolean.other(), numberOfOccurrences + 1));
            }
            return new TaggableFilter(children, treeBoolean);
        }
        else if (object.contains(treeBoolean.other().separator()))
        {
            // We need to keep splitting
            children.add(convert(object, treeBoolean.other(), numberOfOccurrences + 1));
            return new TaggableFilter(children, treeBoolean);
        }
        else
        {
            final String definition = split.get(0);
            final Predicate<Taggable> simple = simple(definition);
            return new TaggableFilter(simple, definition);
        }
    }

    private String escaped(final String value)
    {
        if (TreeBoolean.OR.separator().equals(value))
        {
            return "\\" + TreeBoolean.OR.separator();
        }
        return value;
    }

    private int numberOfSeparators(final int numberOfOccurrences)
    {
        return numberOfOccurrences / 2 + 1;
    }

    private String regex(final String character, final int numberOfOccurrences)
    {
        if (numberOfOccurrences < 1)
        {
            throw new CoreException("Invalid number of occurences for pattern with {}: {}",
                    character, numberOfOccurrences);
        }
        // For || an example is "(?<!\|)\|{2}(?!\|)"
        // One look before and one lookahead
        final String escapedCharacter = escaped(character);
        final String negativeLookBefore = "(?<!" + escapedCharacter + ")";
        String characterTimesNumber = escapedCharacter + "{" + numberOfOccurrences + "}";
        if ("\\|{2}".equals(characterTimesNumber))
        {
            // For backwards compatibility, use the ^ as a replacement option for ||
            characterTimesNumber = "(" + characterTimesNumber + "|\\^)";
        }
        final String negativeLookAhead = "(?!" + escapedCharacter + ")";
        return negativeLookBefore + characterTimesNumber + negativeLookAhead;
    }

    @SuppressWarnings("unchecked")
    private Predicate<Taggable> simple(final String simple)
    {
        final StringList split = StringList.split(simple, KEY_VALUE_SEPARATOR);
        if (split.size() != 2)
        {
            throw new CoreException("Taggable filter definition \"{}\" is invalid.", simple);
        }
        final String key = split.get(0);
        final StringList values = StringList.split(split.get(1), VALUES_SEPARATOR);
        return (Serializable & Predicate<Taggable>) taggable -> taggable.containsValue(key, values);
    }
}

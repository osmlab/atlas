package org.openstreetmap.atlas.utilities.command.documentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.utilities.command.AbstractOSMSubcommand;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionArgumentType;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.SimpleOption;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;
import org.openstreetmap.atlas.utilities.command.terminal.TTYStringBuilder;

/**
 * @author lcram
 */
public final class DocumentationFormatter
{
    private static final int MAXIMUM_COLUMN = 80;
    private static final int INDENTATION_WIDTH = 4;

    /**
     * Call
     * {@link DocumentationFormatter#addParagraphWithLineWrappingAtExactIndentation(int, int, String, TTYStringBuilder)},
     * but compute the exact indentation width by multiplying the supplied indentationLevel with the
     * default INDENTATION_WIDTH.
     */
    public static void addParagraphWithLineWrapping(final int indentationLevel,
            final int maximumColumn, final String string, final TTYStringBuilder builder)
    {
        DocumentationFormatter.addParagraphWithLineWrappingAtExactIndentation(
                indentationLevel * INDENTATION_WIDTH, maximumColumn, string, builder);
    }

    /**
     * Add a string to the builder with a given number of indentation spaces and a given maximum
     * column width. The string will be automatically word-tokenized, ie. it will be split on
     * whitespace. This means that multiple consecutive whitespace will be lost. Uses a simple
     * greedy algorithm for word wrap calculation.
     *
     * @see "https://en.wikipedia.org/wiki/Line_wrap_and_word_wrap#Minimum_number_of_lines"
     * @param exactIndentation
     *            the exact number of indentation spaces
     * @param maximumColumn
     *            the max column width
     * @param string
     *            the string to display
     * @param builder
     *            the builder to be modified
     */
    public static void addParagraphWithLineWrappingAtExactIndentation(final int exactIndentation,
            final int maximumColumn, final String string, final TTYStringBuilder builder)
    {
        final int lineWidth = maximumColumn - exactIndentation;
        int spaceLeft = lineWidth;
        final String[] words = string.split("\\s+");
        for (final String word : words)
        {
            if (word.length() + " ".length() > spaceLeft)
            {
                builder.newline();
                indentBuilderToExact(exactIndentation, builder);
                builder.append(word + " ");
                spaceLeft = lineWidth - word.length();
            }
            else
            {
                builder.append(word + " ");
                spaceLeft = spaceLeft - (word.length() + " ".length());
            }
        }
    }

    public static String generateTextForOptionsSection(final Set<SimpleOption> options,
            final TTYStringBuilder builder)
    {
        final List<SimpleOption> sortedOptions = new ArrayList<>(options);
        Collections.sort(sortedOptions);
        for (final SimpleOption option : sortedOptions)
        {
            indentBuilderToLevel(1, builder);
            builder.append(SimpleOptionAndArgumentParser.LONG_FORM_PREFIX + option.getLongForm(),
                    TTYAttribute.BOLD);
            final OptionArgumentType argumentType = option.getArgumentType();
            if (argumentType == OptionArgumentType.OPTIONAL)
            {
                // TODO is it always safe to unwrap this optional?
                builder.append("[" + SimpleOptionAndArgumentParser.OPTION_ARGUMENT_DELIMITER
                        + option.getArgumentHint().get() + "]");
            }
            else if (argumentType == OptionArgumentType.REQUIRED)
            {
                // TODO is it always safe to unwrap this optional?
                builder.append(SimpleOptionAndArgumentParser.OPTION_ARGUMENT_DELIMITER + "<"
                        + option.getArgumentHint().get() + ">");
            }
            if (option.getShortForm().isPresent())
            {
                builder.append(", ");
                // TODO short options should also show args if present
                builder.append(SimpleOptionAndArgumentParser.SHORT_FORM_PREFIX
                        + option.getShortForm().get().toString(), TTYAttribute.BOLD);
            }
            builder.newline();
            indentBuilderToLevel(2, builder);
            addParagraphWithLineWrapping(2, MAXIMUM_COLUMN, option.getDescription(), builder);
            builder.newline().newline();
        }

        return builder.toString();
    }

    public static void generateTextForSynopsisSection(final String programName,
            final Set<SimpleOption> options, final Map<String, ArgumentArity> argumentArities,
            final Map<String, ArgumentOptionality> argumentOptionalities,
            final TTYStringBuilder builder)
    {
        indentBuilderToLevel(1, builder);
        builder.append(programName, TTYAttribute.UNDERLINE).append(" ")
                .append("[" + SimpleOptionAndArgumentParser.LONG_FORM_PREFIX
                        + AbstractOSMSubcommand.DEFAULT_HELP_LONG + "]")
                .newline();
        indentBuilderToLevel(1, builder);
        builder.append(programName, TTYAttribute.UNDERLINE).append(" ")
                .append("[" + SimpleOptionAndArgumentParser.LONG_FORM_PREFIX
                        + AbstractOSMSubcommand.DEFAULT_VERSION_LONG + "]")
                .newline();
        indentBuilderToLevel(1, builder);
        builder.append(programName, TTYAttribute.UNDERLINE).append(" ");
        final StringBuilder paragraph = new StringBuilder();

        // add all the options
        final List<SimpleOption> sortedOptions = new ArrayList<>(options);
        Collections.sort(sortedOptions);
        for (final SimpleOption option : sortedOptions)
        {
            // skip --help and --version, these are special hardcoded cases handled above
            if (AbstractOSMSubcommand.DEFAULT_HELP_LONG.equals(option.getLongForm())
                    || AbstractOSMSubcommand.DEFAULT_VERSION_LONG.equals(option.getLongForm()))
            {
                continue;
            }
            paragraph.append(
                    "[" + SimpleOptionAndArgumentParser.LONG_FORM_PREFIX + option.getLongForm());
            final OptionArgumentType argumentType = option.getArgumentType();
            if (argumentType == OptionArgumentType.OPTIONAL)
            {
                // TODO is it always safe to unwrap this optional?
                paragraph.append("[" + SimpleOptionAndArgumentParser.OPTION_ARGUMENT_DELIMITER
                        + option.getArgumentHint().get() + "]");
            }
            else if (argumentType == OptionArgumentType.REQUIRED)
            {
                // TODO is it always safe to unwrap this optional?
                paragraph.append(SimpleOptionAndArgumentParser.OPTION_ARGUMENT_DELIMITER + "<"
                        + option.getArgumentHint().get() + ">");
            }
            paragraph.append("] ");
        }

        for (final String hint : argumentArities.keySet())
        {
            if (argumentOptionalities.get(hint) == ArgumentOptionality.OPTIONAL)
            {
                paragraph.append("[");
            }
            else if (argumentOptionalities.get(hint) == ArgumentOptionality.REQUIRED)
            {
                paragraph.append("<");
            }

            paragraph.append(hint);
            if (argumentArities.get(hint) == ArgumentArity.VARIADIC)
            {
                paragraph.append("...");
            }

            if (argumentOptionalities.get(hint) == ArgumentOptionality.OPTIONAL)
            {
                paragraph.append("] ");
            }
            else if (argumentOptionalities.get(hint) == ArgumentOptionality.REQUIRED)
            {
                paragraph.append("> ");
            }
        }

        final int exactIndentation = 1 * INDENTATION_WIDTH + programName.length() + " ".length();
        addParagraphWithLineWrappingAtExactIndentation(exactIndentation, MAXIMUM_COLUMN,
                paragraph.toString(), builder);
    }

    /**
     * Add indentation with a exact width to the builder. This indentation is reset once a newline
     * is appended to the builder.
     *
     * @param exactIndentation
     *            the exact indentation
     * @param builder
     *            the builder
     */
    public static void indentBuilderToExact(final int exactIndentation,
            final TTYStringBuilder builder)
    {
        for (int count = 0; count < exactIndentation; count++)
        {
            builder.append(" ");
        }
    }

    /**
     * Add indentation with a given level to the builder. This indentation is reset once a newline
     * is appended to the builder.
     *
     * @param indentationLevel
     *            the indentation level
     * @param builder
     *            the builder
     */
    public static void indentBuilderToLevel(final int indentationLevel,
            final TTYStringBuilder builder)
    {
        DocumentationFormatter.indentBuilderToExact(indentationLevel * INDENTATION_WIDTH, builder);
    }

    private DocumentationFormatter()
    {

    }
}

package org.openstreetmap.atlas.utilities.command.documentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.utilities.command.AbstractAtlasShellToolsCommand;
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
    public static final int DEFAULT_MAXIMUM_COLUMN = 80;
    public static final int INDENTATION_WIDTH = 4;

    public static final int DEFAULT_CODE_INDENT_LEVEL = 2;
    public static final int DEFAULT_PARAGRAPH_INDENT_LEVEL = 1;
    private static final int DEFAULT_INNER_PARAGRAPH_INDENT_LEVEL = 2;

    /**
     * Call
     * {@link DocumentationFormatter#addCodeBlockAtExactIndentation(int, String, TTYStringBuilder)},
     * but compute the exact indentation width by multiplying the supplied indentationLevel with the
     * default INDENTATION_WIDTH.
     *
     * @param indentationLevel
     *            the indentation level
     * @param string
     *            the code block string
     * @param builder
     *           the builder to be modified
     */
    public static void addCodeBlock(final int indentationLevel, final String string,
            final TTYStringBuilder builder)
    {
        DocumentationFormatter.addCodeBlockAtExactIndentation(indentationLevel * INDENTATION_WIDTH,
                string, builder);
    }

    /**
     * Add a string to the builder with a given number of indentation spaces and a given maximum
     * column width. The string will be treated as a code block, ie. it will not have any special
     * formatting applied to it.
     *
     * @param exactIndentation
     *            the exact number of indentation spaces
     * @param string
     *            the string to display
     * @param builder
     *            the builder to be modified
     */
    public static void addCodeBlockAtExactIndentation(final int exactIndentation,
            final String string, final TTYStringBuilder builder)
    {
        // TODO this currently fails for multiline codeblocks
        indentBuilderToExact(exactIndentation, builder);
        builder.append(string);
    }

    /**
     * Call
     * {@link DocumentationFormatter#addParagraphWithLineWrappingAtExactIndentation(int, int, String, TTYStringBuilder, boolean)},
     * but compute the exact indentation width by multiplying the supplied indentationLevel with the
     * default INDENTATION_WIDTH.
     *
     * @param indentationLevel
     *            the indentation level
     * @param maximumColumn
     *            the max column to wrap at
     * @param string
     *            the code block string
     * @param builder
     *           the builder to be modified
     * @param indentFirstLine
     *            decide to indent the first line
     */
    public static void addParagraphWithLineWrapping(final int indentationLevel,
            final int maximumColumn, final String string, final TTYStringBuilder builder,
            final boolean indentFirstLine)
    {
        DocumentationFormatter.addParagraphWithLineWrappingAtExactIndentation(
                indentationLevel * INDENTATION_WIDTH, maximumColumn, string, builder,
                indentFirstLine);
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
     * @param indentFirstLine
     *            whether or not to indent the first line in the paragraph
     */
    public static void addParagraphWithLineWrappingAtExactIndentation(final int exactIndentation,
            final int maximumColumn, final String string, final TTYStringBuilder builder,
            final boolean indentFirstLine)
    {
        final int lineWidth = maximumColumn - exactIndentation;
        int spaceLeft = lineWidth;
        final String[] words = string.split("\\s+");
        boolean firstIteration = true;

        if (indentFirstLine)
        {
            indentBuilderToExact(exactIndentation, builder);
        }
        for (final String word : words)
        {
            if (word.length() + " ".length() > spaceLeft)
            {
                /*
                 * This is a special edge case that can occur if the first word of the documentation
                 * is longer than the line length: if we are on the first iteration, we already
                 * indented so just skip this extra indentation step. We also do not need a newline
                 * on the first iteration.
                 */
                if (!firstIteration)
                {
                    builder.newline();
                    indentBuilderToExact(exactIndentation, builder);
                }
                builder.append(word + " ");
                spaceLeft = lineWidth - word.length();
            }
            else
            {
                builder.append(word + " ");
                spaceLeft = spaceLeft - (word.length() + " ".length());
            }
            firstIteration = false;
        }
    }

    public static String generateTextForOptionsSection(final int maximumColumn,
            final Set<SimpleOption> options, final TTYStringBuilder builder)
    {
        final List<SimpleOption> sortedOptions = new ArrayList<>(options);
        Collections.sort(sortedOptions);
        for (final SimpleOption option : sortedOptions)
        {
            indentBuilderToLevel(DEFAULT_PARAGRAPH_INDENT_LEVEL, builder);
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
                builder.append(SimpleOptionAndArgumentParser.SHORT_FORM_PREFIX
                        + option.getShortForm().get().toString(), TTYAttribute.BOLD);
                if (argumentType == OptionArgumentType.OPTIONAL)
                {
                    // TODO is it always safe to unwrap this optional?
                    builder.append("[" + option.getArgumentHint().get() + "]");
                }
                else if (argumentType == OptionArgumentType.REQUIRED)
                {
                    // TODO is it always safe to unwrap this optional?
                    builder.append("<" + option.getArgumentHint().get() + ">");
                }
            }
            builder.newline();
            addParagraphWithLineWrapping(DEFAULT_INNER_PARAGRAPH_INDENT_LEVEL, maximumColumn,
                    option.getDescription(), builder, true);
            builder.newline().newline();
        }

        return builder.toString();
    }

    public static void generateTextForSynopsisSection(final String programName,
            final int maximumColumn, final Set<SimpleOption> options,
            final Map<String, ArgumentArity> argumentArities,
            final Map<String, ArgumentOptionality> argumentOptionalities,
            final TTYStringBuilder builder)
    {
        indentBuilderToLevel(DEFAULT_PARAGRAPH_INDENT_LEVEL, builder);
        builder.append(programName, TTYAttribute.UNDERLINE).append(" ")
                .append("[" + SimpleOptionAndArgumentParser.LONG_FORM_PREFIX
                        + AbstractAtlasShellToolsCommand.DEFAULT_HELP_LONG + "]")
                .newline();
        indentBuilderToLevel(DEFAULT_PARAGRAPH_INDENT_LEVEL, builder);
        builder.append(programName, TTYAttribute.UNDERLINE).append(" ")
                .append("[" + SimpleOptionAndArgumentParser.LONG_FORM_PREFIX
                        + AbstractAtlasShellToolsCommand.DEFAULT_VERSION_LONG + "]")
                .newline();
        indentBuilderToLevel(DEFAULT_PARAGRAPH_INDENT_LEVEL, builder);
        builder.append(programName, TTYAttribute.UNDERLINE).append(" ");
        final StringBuilder paragraph = new StringBuilder();

        // add all the options
        final List<SimpleOption> sortedOptions = new ArrayList<>(options);
        Collections.sort(sortedOptions);
        for (final SimpleOption option : sortedOptions)
        {
            // skip --help and --version, these are special hardcoded cases handled above
            if (AbstractAtlasShellToolsCommand.DEFAULT_HELP_LONG.equals(option.getLongForm())
                    || AbstractAtlasShellToolsCommand.DEFAULT_VERSION_LONG.equals(option.getLongForm()))
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

        final int exactIndentation = DEFAULT_PARAGRAPH_INDENT_LEVEL * INDENTATION_WIDTH
                + programName.length() + " ".length();
        addParagraphWithLineWrappingAtExactIndentation(exactIndentation, maximumColumn,
                paragraph.toString(), builder, false);
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

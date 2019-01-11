package org.openstreetmap.atlas.utilities.command.documentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionArgumentType;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.SimpleOption;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;
import org.openstreetmap.atlas.utilities.command.terminal.TTYStringBuilder;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * @author lcram
 */
public final class DocumentationFormatter
{
    public static final int DEFAULT_MAXIMUM_COLUMN = 80;

    public static final int DEFAULT_CODE_INDENT_LEVEL = 2;
    public static final int DEFAULT_CODE_INDENT_WIDTH = 4;

    public static final int DEFAULT_PARAGRAPH_INDENT_LEVEL = 1;
    public static final int DEFAULT_INNER_PARAGRAPH_INDENT_LEVEL = 2;
    public static final int DEFAULT_PARAGRAPH_INDENT_WIDTH = 4;

    /**
     * Call
     * {@link DocumentationFormatter#addCodeLineAtExactIndentation(int, String, TTYStringBuilder)},
     * but compute the exact indentation width by multiplying the supplied indentationLevel with the
     * default INDENTATION_WIDTH.
     *
     * @param indentationLevel
     *            the indentation level
     * @param string
     *            the code block string
     * @param builder
     *            the builder to be modified
     */
    public static void addCodeLine(final int indentationLevel, final String string,
            final TTYStringBuilder builder)
    {
        DocumentationFormatter.addCodeLineAtExactIndentation(
                indentationLevel * DEFAULT_CODE_INDENT_WIDTH, string, builder);
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
    public static void addCodeLineAtExactIndentation(final int exactIndentation,
            final String string, final TTYStringBuilder builder)
    {
        builder.pushExactIndentWidth(exactIndentation).append(string).popIndentation();
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
     *            the builder to be modified
     * @param indentFirstLine
     *            decide to indent the first line
     */
    public static void addParagraphWithLineWrapping(final int indentationLevel,
            final int maximumColumn, final String string, final TTYStringBuilder builder,
            final boolean indentFirstLine)
    {
        DocumentationFormatter.addParagraphWithLineWrappingAtExactIndentation(
                indentationLevel * DEFAULT_PARAGRAPH_INDENT_WIDTH, maximumColumn, string, builder,
                indentFirstLine);
    }

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
            builder.pushExactIndentWidth(exactIndentation);
        }
        else
        {
            builder.pushExactIndentWidth(0);
        }
        for (final String word : words)
        {
            // Word fits exactly in the remaining space
            if (word.length() == spaceLeft)
            {
                builder.append(word).pushExactIndentWidth(0).newline()
                        .pushExactIndentWidth(exactIndentation);
                spaceLeft = lineWidth;
            }
            // Word plus a whitespace is longer than the remaining space
            else if (word.length() + " ".length() > spaceLeft)
            {
                /*
                 * This is a special edge case that can occur if the first word of the documentation
                 * is longer than the line length: if we are on the first iteration, we already
                 * new-lined and indented so just skip these steps.
                 */
                if (!firstIteration)
                {
                    builder.newline();
                    builder.pushExactIndentWidth(exactIndentation);
                }
                builder.append(word + " ").pushExactIndentWidth(0);
                spaceLeft = lineWidth - (word.length() + " ".length());
            }
            // Word plus a whitespace fits in the remaining space
            else
            {
                builder.append(word + " ").pushExactIndentWidth(0);
                spaceLeft = spaceLeft - (word.length() + " ".length());
            }
            firstIteration = false;
        }
    }

    public static void generateTextForGenericSection(final String sectionName,
            final int maximumColumn, final TTYStringBuilder builder,
            final DocumentationRegistrar registrar)
    {
        final List<Tuple<DocumentationFormatType, String>> sectionContents = registrar
                .getSectionContents(sectionName);
        final List<Tuple<DocumentationFormatType, String>> sectionContentsFiltered = new ArrayList<>();
        // Filter out any empty sections
        for (final Tuple<DocumentationFormatType, String> contents : sectionContents)
        {
            if (!contents.getSecond().isEmpty())
            {
                sectionContentsFiltered.add(contents);
            }
        }

        builder.clearIndentationStack();
        builder.append(sectionName, TTYAttribute.BOLD).newline();
        for (int index = 0; index < sectionContentsFiltered.size(); index++)
        {
            final Tuple<DocumentationFormatType, String> contents = sectionContentsFiltered
                    .get(index);
            final DocumentationFormatType type = contents.getFirst();
            final String text = contents.getSecond();
            if (type == DocumentationFormatType.CODE)
            {
                DocumentationFormatter.addCodeLine(DocumentationFormatter.DEFAULT_CODE_INDENT_LEVEL,
                        text, builder);
                builder.newline();
            }
            else if (type == DocumentationFormatType.PARAGRAPH)
            {
                DocumentationFormatter.addParagraphWithLineWrapping(
                        DocumentationFormatter.DEFAULT_PARAGRAPH_INDENT_LEVEL, maximumColumn, text,
                        builder, true);
                builder.newline();
            }
            // Add an extra newline unless we are on the last element
            if (index < sectionContentsFiltered.size() - 1)
            {
                builder.newline();
            }
        }
    }

    public static void generateTextForNameSection(final String name, final String simpleDescription,
            final TTYStringBuilder builder)
    {
        builder.append("NAME", TTYAttribute.BOLD).newline();
        builder.clearIndentationStack();
        builder.withLevelWidth(DEFAULT_PARAGRAPH_INDENT_WIDTH);
        builder.pushIndentLevel(DEFAULT_PARAGRAPH_INDENT_LEVEL)
                .append(name + " -- " + simpleDescription).popIndentation();
        builder.newline();
    }

    public static String generateTextForOptionsSection(final int maximumColumn,
            final Set<SimpleOption> options, final TTYStringBuilder builder)
    {
        final List<SimpleOption> sortedOptions = new ArrayList<>(options);
        Collections.sort(sortedOptions);
        builder.clearIndentationStack();
        builder.withLevelWidth(DEFAULT_PARAGRAPH_INDENT_WIDTH);
        builder.append("OPTIONS", TTYAttribute.BOLD).newline();
        for (int index = 0; index < sortedOptions.size(); index++)
        {
            final SimpleOption option = sortedOptions.get(index);
            builder.pushIndentLevel(DEFAULT_PARAGRAPH_INDENT_LEVEL)
                    .append(SimpleOptionAndArgumentParser.LONG_FORM_PREFIX + option.getLongForm(),
                            TTYAttribute.BOLD)
                    .popIndentation();
            final OptionArgumentType argumentType = option.getArgumentType();
            if (argumentType == OptionArgumentType.OPTIONAL)
            {
                builder.append("[" + SimpleOptionAndArgumentParser.OPTION_ARGUMENT_DELIMITER
                        + option.getArgumentHint().orElseThrow(AtlasShellToolsException::new)
                        + "]");
            }
            else if (argumentType == OptionArgumentType.REQUIRED)
            {
                builder.append(SimpleOptionAndArgumentParser.OPTION_ARGUMENT_DELIMITER + "<"
                        + option.getArgumentHint().orElseThrow(AtlasShellToolsException::new)
                        + ">");
            }
            if (option.getShortForm().isPresent())
            {
                builder.append(", ");
                builder.append(
                        SimpleOptionAndArgumentParser.SHORT_FORM_PREFIX + option.getShortForm()
                                .orElseThrow(AtlasShellToolsException::new).toString(),
                        TTYAttribute.BOLD);
                if (argumentType == OptionArgumentType.OPTIONAL)
                {
                    builder.append("["
                            + option.getArgumentHint().orElseThrow(AtlasShellToolsException::new)
                            + "]");
                }
                else if (argumentType == OptionArgumentType.REQUIRED)
                {
                    builder.append("<"
                            + option.getArgumentHint().orElseThrow(AtlasShellToolsException::new)
                            + ">");
                }
            }
            builder.newline();
            addParagraphWithLineWrapping(DEFAULT_INNER_PARAGRAPH_INDENT_LEVEL, maximumColumn,
                    option.getDescription(), builder, true);
            builder.newline();
            // Add an extra newline when we are not on the last element
            if (index < sortedOptions.size() - 1)
            {
                builder.newline();
            }
        }

        return builder.toString();
    }

    public static void generateTextForSynopsisSection(final String programName, // NOSONAR
            final int maximumColumn, final Map<Integer, Set<SimpleOption>> optionsWithContext,
            final Set<Integer> contexts,
            final Map<Integer, Map<String, ArgumentArity>> argumentArities,
            final Map<Integer, Map<String, ArgumentOptionality>> argumentOptionalities,
            final TTYStringBuilder builder)
    {
        builder.append("SYNOPSIS", TTYAttribute.BOLD).newline();
        builder.clearIndentationStack();
        builder.withLevelWidth(DEFAULT_PARAGRAPH_INDENT_WIDTH);
        for (final Integer context : contexts)
        {
            builder.pushIndentLevel(DEFAULT_PARAGRAPH_INDENT_LEVEL)
                    .append(programName, TTYAttribute.UNDERLINE).popIndentation().append(" ");
            final StringBuilder paragraph = new StringBuilder();

            // add all the options
            final List<SimpleOption> sortedOptions = new ArrayList<>(
                    optionsWithContext.getOrDefault(context, new HashSet<>()));
            Collections.sort(sortedOptions);
            for (final SimpleOption option : sortedOptions)
            {
                if (option.getOptionality() == OptionOptionality.OPTIONAL)
                {
                    paragraph.append("[");
                }
                paragraph.append(
                        SimpleOptionAndArgumentParser.LONG_FORM_PREFIX + option.getLongForm());
                final OptionArgumentType argumentType = option.getArgumentType();
                if (argumentType == OptionArgumentType.OPTIONAL)
                {
                    paragraph.append("[" + SimpleOptionAndArgumentParser.OPTION_ARGUMENT_DELIMITER
                            + option.getArgumentHint().orElseThrow(AtlasShellToolsException::new)
                            + "]");
                }
                else if (argumentType == OptionArgumentType.REQUIRED)
                {
                    paragraph.append(SimpleOptionAndArgumentParser.OPTION_ARGUMENT_DELIMITER + "<"
                            + option.getArgumentHint().orElseThrow(AtlasShellToolsException::new)
                            + ">");
                }
                if (option.getOptionality() == OptionOptionality.OPTIONAL)
                {
                    paragraph.append("]");
                }
                paragraph.append(" ");
            }

            // now add all the arguments
            for (final String hint : argumentArities.getOrDefault(context, new HashMap<>())
                    .keySet())
            {
                if (argumentOptionalities.get(context).get(hint) == ArgumentOptionality.OPTIONAL)
                {
                    paragraph.append("[");
                }
                else if (argumentOptionalities.get(context)
                        .get(hint) == ArgumentOptionality.REQUIRED)
                {
                    paragraph.append("<");
                }

                paragraph.append(hint);
                if (argumentArities.get(context).get(hint) == ArgumentArity.VARIADIC)
                {
                    paragraph.append("...");
                }

                if (argumentOptionalities.get(context).get(hint) == ArgumentOptionality.OPTIONAL)
                {
                    paragraph.append("] ");
                }
                else if (argumentOptionalities.get(context)
                        .get(hint) == ArgumentOptionality.REQUIRED)
                {
                    paragraph.append("> ");
                }
            }

            final int exactIndentation = DEFAULT_PARAGRAPH_INDENT_LEVEL
                    * DEFAULT_PARAGRAPH_INDENT_WIDTH + programName.length() + " ".length();
            addParagraphWithLineWrappingAtExactIndentation(exactIndentation, maximumColumn,
                    paragraph.toString(), builder, false);
            builder.pushIndentLevel(0).newline();
        }
    }

    private DocumentationFormatter()
    {

    }
}

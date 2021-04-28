package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * @author lcram
 */
public class TaggableMatcherPrinterCommand extends AbstractAtlasShellToolsCommand
{
    private static final String REVERSE_OPTION_LONG = "reverse";
    private static final String REVERSE_OPTION_DESCRIPTION = "Convert an old-style TaggableFilter into a TaggableMatcher.";

    private static final String FILTERS_ARGUMENT = "filters";
    private static final String MATCHERS_ARGUMENT = "matchers";

    private static final int REVERSE_CONTEXT = 4;

    public static void main(final String[] args)
    {
        new TaggableMatcherPrinterCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        if (this.getOptionAndArgumentDelegate().getParserContext() == REVERSE_CONTEXT)
        {
            executeReverseContext();
        }
        else
        {
            executeDefaultContext();
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "print-matcher";
    }

    @Override
    public String getSimpleDescription()
    {
        return "print a TaggableMatcher as a tree";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", AtlasSearchCommand.class
                .getResourceAsStream("TaggableMatcherPrinterCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", AtlasSearchCommand.class
                .getResourceAsStream("TaggableMatcherPrinterCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument(MATCHERS_ARGUMENT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        registerArgument(FILTERS_ARGUMENT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED,
                REVERSE_CONTEXT);
        registerOption(REVERSE_OPTION_LONG, REVERSE_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                REVERSE_CONTEXT);
        super.registerOptionsAndArguments();
    }

    private void executeDefaultContext()
    {
        final List<String> definitions = this.getOptionAndArgumentDelegate()
                .getVariadicArgument(MATCHERS_ARGUMENT);
        for (int i = 0; i < definitions.size(); i++)
        {
            final String definition = definitions.get(i);
            try
            {
                final TaggableMatcher matcher = TaggableMatcher.from(definition);
                this.getCommandOutputDelegate().printStdout(matcher.prettyPrintTree());
                if (matcher.lengthOfLongestLineForPrintedTree() > this.getMaximumColumn())
                {
                    this.getCommandOutputDelegate()
                            .printlnWarnMessage("tree was too big for detected terminal width");
                    this.getCommandOutputDelegate().printlnCommandMessage(
                            "try piping into `less -S' to disable line-wrapping");
                }
            }
            catch (final CoreException exception)
            {
                if (exception.getMessage().contains("invalid nested equality operators"))
                {
                    this.getCommandOutputDelegate().printlnErrorMessage(
                            "definition `" + definition + "' contained nested equality operators");
                }
                else
                {
                    throw exception;
                }
            }

            // Print an extra newline between trees, but not for the last tree
            if (i < definitions.size() - 1)
            {
                this.getCommandOutputDelegate().printlnStdout("");
            }
        }
    }

    private void executeReverseContext()
    {
        final List<String> definitions = this.getOptionAndArgumentDelegate()
                .getVariadicArgument(FILTERS_ARGUMENT);
        for (final String definition : definitions)
        {
            try
            {
                this.getCommandOutputDelegate().printlnStdout(TaggableFilter
                        .forDefinition(definition).convertToTaggableMatcher().getDefinition());
            }
            catch (final Exception exception)
            {
                this.getCommandOutputDelegate().printlnErrorMessage(exception.getMessage());
            }
        }
    }
}

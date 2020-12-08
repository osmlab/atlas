package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.filters.matcher.TaggableMatcher;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class TaggableMatcherPrinterCommand extends AbstractAtlasShellToolsCommand
{
    public static void main(final String[] args)
    {
        new TaggableMatcherPrinterCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        final List<String> definitions = this.getOptionAndArgumentDelegate()
                .getVariadicArgument("matchers");
        for (final String definition : definitions)
        {
            this.getCommandOutputDelegate().printlnStdout(definition, TTYAttribute.BOLD,
                    TTYAttribute.GREEN);
            try
            {
                final TaggableMatcher matcher = TaggableMatcher.from(definition);
                this.getCommandOutputDelegate().printlnStdout(matcher.prettyPrintTree());
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
            this.getCommandOutputDelegate().printlnStdout("");
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
        registerArgument("matchers", ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        super.registerOptionsAndArguments();
    }
}

package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommandTemplate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.ListOfNumbersTemplate;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * A command to test the {@link AbstractAtlasShellToolsCommandTemplate} feature.
 * 
 * @author lcram
 */
public class TemplateTestCommand extends AbstractAtlasShellToolsCommand
{
    private static final int REVERSE_CONTEXT = 4;

    public static void main(final String[] args)
    {
        new TemplateTestCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        if (this.getOptionAndArgumentDelegate()
                .getParserContext() == AbstractAtlasShellToolsCommand.DEFAULT_CONTEXT)
        {
            final List<Integer> listOfNumbers = ListOfNumbersTemplate.getListOfNumbers(this);
            if (listOfNumbers.isEmpty())
            {
                this.getCommandOutputDelegate().printlnErrorMessage("failed to parse number list!");
                return 1;
            }
            this.getCommandOutputDelegate().printlnStdout(listOfNumbers.toString(),
                    TTYAttribute.GREEN);
        }
        else
        {
            this.getCommandOutputDelegate().printlnStdout("Using reverse context!",
                    TTYAttribute.GREEN);
        }
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "template-test";
    }

    @Override
    public String getSimpleDescription()
    {
        return "test the Atlas Shell Tools template feature";
    }

    @Override
    public void registerManualPageSections()
    {
        registerManualPageSectionsFromTemplate(new ListOfNumbersTemplate());
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionsAndArgumentsFromTemplate(
                new ListOfNumbersTemplate(AbstractAtlasShellToolsCommand.DEFAULT_CONTEXT));
        registerOption("reverse", "Perform this operation in reverse.", OptionOptionality.REQUIRED,
                REVERSE_CONTEXT);
        super.registerOptionsAndArguments();
    }
}

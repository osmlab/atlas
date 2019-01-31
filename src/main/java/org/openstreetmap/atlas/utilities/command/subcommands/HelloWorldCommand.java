package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * @author lcram
 */
public class HelloWorldCommand extends AbstractAtlasShellToolsCommand
{
    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new HelloWorldCommand().runSubcommandAndExit(args);
    }

    public HelloWorldCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        this.outputDelegate.printStdout("Hello, "
                + this.optionAndArgumentDelegate.getOptionArgument("name").orElse("world") + "!\n");
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "hello-world";
    }

    @Override
    public String getSimpleDescription()
    {
        return "a simple subcommand that prints \"Hello, world!\" and exits";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", HelloWorldCommand.class
                .getResourceAsStream("HelloWorldCommandDescriptionSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument("name", "Your name for the greeting.",
                OptionOptionality.OPTIONAL, "name");
        super.registerOptionsAndArguments();
    }
}

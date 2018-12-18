package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentFetcher;

/**
 * @author lcram
 */
public class HelloWorldCommand extends AbstractAtlasShellToolsCommand
{
    private static final String DESCRIPTION_SECTION = "HelloWorldCommandDescriptionSection.txt";

    private final OptionAndArgumentFetcher fetcher;
    private final CommandOutputDelegate output;

    public static void main(final String[] args)
    {
        new HelloWorldCommand().runSubcommandAndExit(args);
    }

    public HelloWorldCommand()
    {
        this.fetcher = this.getOptionAndArgumentFetcher();
        this.output = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        this.output.printStdout(
                "Hello, " + this.fetcher.getOptionArgument("name").orElse("world") + "!\n");
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
        addManualPageSection("DESCRIPTION",
                HelloWorldCommand.class.getResourceAsStream(DESCRIPTION_SECTION));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument("name", "Your name for the greeting.", "name");
    }
}

package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.AbstractAtlasShellToolsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class HelloWorldCommand extends AbstractAtlasShellToolsCommand
{
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldCommand.class);

    private static final String DESCRIPTION_SECTION = "HelloWorldCommandDescriptionSection.txt";

    public static void main(final String[] args)
    {
        new HelloWorldCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        printStdout("Hello, " + getOptionArgument("name").orElse("world") + "!\n");
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

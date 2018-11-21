package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.AbstractOSMSubcommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class HelloWorldSubcommand extends AbstractOSMSubcommand
{
    private static final Logger logger = LoggerFactory.getLogger(HelloWorldSubcommand.class);

    private static final String NAME_OPTION = "name";

    public static void main(final String[] args)
    {
        new HelloWorldSubcommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        printStdout("Hello, " + getOptionArgument(NAME_OPTION).orElse("world") + "!\n");
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "HelloWorld";
    }

    @Override
    public String getSimpleDescription()
    {
        return "a simple subcommand that prints \"Hello, world!\" and exits";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION");
        addParagraphToSection("DESCRIPTION",
                "Prints a simple greeting. The greeting can be personalized with the '--name' option.");
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(NAME_OPTION, "Your name for the greeting.", "name");
    }
}

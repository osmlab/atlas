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

    public static void main(final String[] args)
    {
        new HelloWorldSubcommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        logger.trace("TRACE message");
        logger.debug("DEBUG message");
        logger.info("INFO message");
        logger.warn("WARN message");
        logger.error("ERROR message");
        System.out.println("Hello, world!");
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
    public void registerOptionsAndArguments()
    {
        // TODO implement
    }
}

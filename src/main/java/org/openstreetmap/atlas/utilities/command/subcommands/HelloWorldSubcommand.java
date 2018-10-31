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
}

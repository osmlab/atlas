package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.AbstractOSMSubcommand;

public class TestSubcommand extends AbstractOSMSubcommand
{
    public static void main(final String[] args)
    {
        new TestSubcommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        System.out.println("test subcommand output");
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "test";
    }

    @Override
    public String getSimpleDescription()
    {
        return "a test subcommand";
    }

    @Override
    public void registerOptionsAndArguments()
    {
        // TODO implement
    }

}

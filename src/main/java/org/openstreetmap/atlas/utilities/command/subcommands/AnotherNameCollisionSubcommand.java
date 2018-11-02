package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.AbstractOSMSubcommand;

public class AnotherNameCollisionSubcommand extends AbstractOSMSubcommand
{
    public static void main(final String[] args)
    {
        new AnotherNameCollisionSubcommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        System.out.println("another name collision command");
        return 0;
    }

    @Override
    public String getName()
    {
        return "test";
    }

    @Override
    public String getSimpleDescription()
    {
        return "a second subcommand with a colliding name";
    }

    @Override
    public void registerOptions()
    {

    }
}

package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.AbstractOSMSubcommand;

public class NameCollisionSubcommand2 extends AbstractOSMSubcommand
{
    public static void main(final String[] args)
    {
        new NameCollisionSubcommand2().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        System.out.println("name collision command 2");
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "nameCollision";
    }

    @Override
    public String getSimpleDescription()
    {
        return "a subcommand with a colliding name";
    }

    @Override
    public void registerOptionsAndArguments()
    {
        // TODO implement
    }

}

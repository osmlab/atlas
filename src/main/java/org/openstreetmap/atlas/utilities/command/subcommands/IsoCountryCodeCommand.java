package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;

public class IsoCountryCodeCommand extends AbstractAtlasShellToolsCommand
{
    public static void main(final String[] args)
    {
        new IsoCountryCodeCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "iso-country-code";
    }

    @Override
    public String getSimpleDescription()
    {
        return "convert ISO country codes to countries and back again";
    }

    @Override
    public void registerManualPageSections()
    {

    }
}

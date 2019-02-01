package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;

public class IdentifierSearchCommand extends AtlasLoaderCommand
{
    public static void main(final String[] args)
    {
        new IdentifierSearchCommand().runSubcommandAndExit(args);
    }

    @Override
    public String getCommandName()
    {
        return "find-id";
    }

    @Override
    public String getSimpleDescription()
    {
        return "find features with given identifier(s) in given atlas(es)";
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName)
    {

    }
}

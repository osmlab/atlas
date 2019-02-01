package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;

/**
 * Search atlases for some given feature identifiers, with various options and restrictions. Based
 * on similar identifier locater commands by cstaylor and bbreithaupt.
 *
 * @author lcram
 * @author cstaylor
 * @author bbreithaupt
 */
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

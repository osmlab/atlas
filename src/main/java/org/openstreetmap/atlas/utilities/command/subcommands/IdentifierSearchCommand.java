package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
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
    private static final String ID_OPTION_LONG = "id";
    private static final String ID_OPTION_DESCRIPTION = "A comma separated list of ids for which to search.";
    private static final String ID_OPTION_HINT = "ids";

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
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", IdentifierSearchCommand.class
                .getResourceAsStream("IdentifierSearchCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", IdentifierSearchCommand.class
                .getResourceAsStream("IdentifierSearchCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(ID_OPTION_LONG, ID_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, ID_OPTION_HINT);
        super.registerOptionsAndArguments();
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName)
    {

    }
}

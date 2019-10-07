package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.MultipleOutputCommand;

/**
 * This command converts our many different file formats to a GeoJSON representation. This may be
 * useful for various visualization software.
 * 
 * @author lcram
 */
public class AnyToGeoJsonCommand extends MultipleOutputCommand
{
    private static final String ATLAS_OPTION_LONG = "atlas";
    private static final String ATLAS_OPTION_DESCRIPTION = "The path to an atlas file to be converted.";
    private static final String ATLAS_OPTION_HINT = "atlas-file";

    private static final String SHARDING_OPTION_LONG = "sharding";
    private static final String SHARDING_OPTION_DESCRIPTION = "The sharding to convert, e.g. dynamic@/Users/foo/my-tree.txt";
    private static final String SHARDING_OPTION_HINT = "type@parameter";

    private static final String BOUNDARY_OPTION_LONG = "boundary";
    private static final String BOUNDARY_OPTION_DESCRIPTION = "The path to a boundary file to be converted.";
    private static final String BOUNDARY_OPTION_HINT = "boundary-file";

    private static final Integer ATLAS_CONTEXT = 3;
    private static final Integer SHARDING_CONTEXT = 4;
    private static final Integer BOUNDARY_CONTEXT = 5;

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new AnyToGeoJsonCommand().runSubcommandAndExit(args);
    }

    public AnyToGeoJsonCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        this.outputDelegate
                .printlnCommandMessage(this.optionAndArgumentDelegate.getParserContext() + "");
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "any2geojson";
    }

    @Override
    public String getSimpleDescription()
    {
        return "convert a custom file format to GeoJSON";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", AtlasSearchCommand.class
                .getResourceAsStream("AnyToGeoJsonCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", AtlasSearchCommand.class
                .getResourceAsStream("AnyToGeoJsonCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(ATLAS_OPTION_LONG, ATLAS_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, ATLAS_OPTION_HINT, ATLAS_CONTEXT);
        registerOptionWithRequiredArgument(SHARDING_OPTION_LONG, SHARDING_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, SHARDING_OPTION_HINT, SHARDING_CONTEXT);
        registerOptionWithRequiredArgument(BOUNDARY_OPTION_LONG, BOUNDARY_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, BOUNDARY_OPTION_HINT, BOUNDARY_CONTEXT);
        super.registerOptionsAndArguments();
    }
}

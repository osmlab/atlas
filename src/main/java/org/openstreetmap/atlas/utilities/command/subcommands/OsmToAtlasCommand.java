package org.openstreetmap.atlas.utilities.command.subcommands;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.testing.TestAtlasHandler;

/**
 * Convert an .osm file of various types to an atlas.
 *
 * @author jklamer
 */
public class OsmToAtlasCommand extends AbstractAtlasShellToolsCommand
{
    private static final String INPUT_OSM_FILE_ARGUMENT = "input-osm-file";
    private static final String OUTPUT_ATLAS_FILE_ARGUMENT = "output-atlas-file";

    private static final String JOSM_OPTION_LONG = "josm";
    private static final String JOSM_OPTION_DESCRIPTION = "Specify if the OSM file is in JOSM format.";

    private static final String COUNTRY_OPTION_LONG = "country";
    private static final String COUNTRY_OPTION_DESCRIPTION = "Specify an ISO3 country code to use for slicing.";
    private static final String COUNTRY_OPTION_HINT = "ISO3";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;

    public static void main(final String[] args)
    {
        new OsmToAtlasCommand().runSubcommandAndExit(args);
    }

    public OsmToAtlasCommand()
    {
        super();
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
    }

    @Override
    public int execute()
    {
        final File osmFile = new File(
                this.optionAndArgumentDelegate.getUnaryArgument(INPUT_OSM_FILE_ARGUMENT)
                        .orElseThrow(AtlasShellToolsException::new),
                this.getFileSystem());
        final File atlasFile = new File(
                this.optionAndArgumentDelegate.getUnaryArgument(OUTPUT_ATLAS_FILE_ARGUMENT)
                        .orElseThrow(AtlasShellToolsException::new),
                this.getFileSystem());
        final boolean useJosmFormat = this.optionAndArgumentDelegate.hasOption(JOSM_OPTION_LONG);

        final Atlas atlas = TestAtlasHandler.getAtlasFromJosmOsmResource(useJosmFormat,
                new InputStreamResource(osmFile::read), osmFile.getName(),
                this.optionAndArgumentDelegate.getOptionArgument(COUNTRY_OPTION_LONG));
        atlas.save(atlasFile);

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "osm2atlas";
    }

    @Override
    public String getSimpleDescription()
    {
        return "convert a .osm file into an Atlas file";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", OsmToAtlasCommand.class
                .getResourceAsStream("OsmToAtlasCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", OsmToAtlasCommand.class
                .getResourceAsStream("OsmToAtlasCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument(INPUT_OSM_FILE_ARGUMENT, ArgumentArity.UNARY,
                ArgumentOptionality.REQUIRED);
        registerArgument(OUTPUT_ATLAS_FILE_ARGUMENT, ArgumentArity.UNARY,
                ArgumentOptionality.REQUIRED);
        registerOption(JOSM_OPTION_LONG, JOSM_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        registerOptionWithRequiredArgument(COUNTRY_OPTION_LONG, COUNTRY_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, COUNTRY_OPTION_HINT);
        super.registerOptionsAndArguments();
    }
}

package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;

/**
 * @author lcram
 */
public class PackedToTextAtlasCommand extends AtlasLoaderCommand
{
    private static final String SAVED_TO = "saved to ";

    private static final String GEOJSON_OPTION_LONG = "geojson";
    private static final Character GEOJSON_OPTION_SHORT = 'g';
    private static final String GEOJSON_OPTION_DESCRIPTION = "Save atlas as GeoJSON.";

    private static final String LDGEOJSON_OPTION_LONG = "ldgeojson";
    private static final Character LDGEOJSON_OPTION_SHORT = 'l';
    private static final String LDGEOJSON_OPTION_DESCRIPTION = "Save atlas as line-delimited GeoJSON.";

    private static final Integer GEOJSON_CONTEXT = 4;
    private static final Integer LDGEOJSON_CONTEXT = 5;

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new PackedToTextAtlasCommand().runSubcommandAndExit(args);
    }

    public PackedToTextAtlasCommand()
    {
        super();
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public String getCommandName()
    {
        return "packed2text";
    }

    @Override
    public String getSimpleDescription()
    {
        return "transform a PackedAtlas into a human-readable format";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", PackedToTextAtlasCommand.class
                .getResourceAsStream("PackedToTextAtlasCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", PackedToTextAtlasCommand.class
                .getResourceAsStream("PackedToTextAtlasCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerEmptyContext(AbstractAtlasShellToolsCommand.DEFAULT_CONTEXT);
        registerOption(GEOJSON_OPTION_LONG, GEOJSON_OPTION_SHORT, GEOJSON_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, GEOJSON_CONTEXT);
        registerOption(LDGEOJSON_OPTION_LONG, LDGEOJSON_OPTION_SHORT, LDGEOJSON_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, LDGEOJSON_CONTEXT);
        super.registerOptionsAndArguments();
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate
                    .printlnCommandMessage("converting " + atlasResource.getPath() + "...");
        }
        try
        {
            writeOutput(atlasFileName, atlas);
        }
        catch (final Exception exception)
        {
            this.outputDelegate.printlnErrorMessage("failed to save text file for "
                    + atlasResource.getPath() + ": " + exception.getMessage());
        }
    }

    private void writeOutput(final String atlasFileName, final Atlas outputAtlas)
    {
        final String fileName = AtlasLoaderCommand.removeSuffixFromFileName(atlasFileName);
        final Path concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                fileName);
        File outputFile = null;

        if (this.optionAndArgumentDelegate
                .getParserContext() == AbstractAtlasShellToolsCommand.DEFAULT_CONTEXT)
        {
            outputFile = new File(concatenatedPath.toAbsolutePath().toString() + FileSuffix.TEXT);
            outputAtlas.saveAsText(outputFile);
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == GEOJSON_CONTEXT)
        {
            outputFile = new File(
                    concatenatedPath.toAbsolutePath().toString() + FileSuffix.GEO_JSON);
            outputAtlas.saveAsGeoJson(outputFile);
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == LDGEOJSON_CONTEXT)
        {
            outputFile = new File(
                    concatenatedPath.toAbsolutePath().toString() + FileSuffix.GEO_JSON);
            outputAtlas.saveAsLineDelimitedGeoJsonFeatures(outputFile, (entity, json) ->
            {
                // Dummy consumer, we don't need to mutate the JSON
            });

        }
        else
        {
            throw new AtlasShellToolsException();
        }

        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate
                    .printlnCommandMessage(SAVED_TO + outputFile.getFile().getAbsolutePath());
        }
    }
}

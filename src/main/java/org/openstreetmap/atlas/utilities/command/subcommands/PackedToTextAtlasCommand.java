package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;

/**
 * @author lcram
 */
public class PackedToTextAtlasCommand extends AbstractAtlasShellToolsCommand
{
    private static final String VERSION = "1.0.0";

    private static final String DESCRIPTION_SECTION = "PackedToTextAtlasCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "PackedToTextAtlasCommandExamplesSection.txt";

    private static final String INPUT_HINT = "input-atlases";

    private static final String OUTPUT_DIRECTORY_OPTION_LONG = "output";
    private static final Character OUTPUT_DIRECTORY_OPTION_SHORT = 'o';
    private static final String OUTPUT_DIRECTORY_OPTION_DESCRIPTION = "Specify an alternate output directory for the text atlas files. If the directory\n"
            + "does not exist, it will be created.";
    private static final String OUTPUT_DIRECTORY_OPTION_HINT = "dir";

    private static final String STRICT_OPTION_LONG = "strict";
    private static final Character STRING_OPTION_SHORT = 's';
    private static final String STRICT_OPTION_DESCRIPTION = "Fail fast if any input atlases are missing.";

    private static final String GEOJSON_OPTION_LONG = "geojson";
    private static final Character GEOJSON_OPTION_SHORT = 'g';
    private static final String GEOJSON_OPTION_DESCRIPTION = "Save atlas as GeoJSON.";

    private static final String LDGEOJSON_OPTION_LONG = "ldgeojson";
    private static final Character LDGEOJSON_OPTION_SHORT = 'l';
    private static final String LDGEOJSON_OPTION_DESCRIPTION = "Save atlas as line-delimited GeoJSON.";

    public static void main(final String[] args)
    {
        new PackedToTextAtlasCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        final List<String> inputAtlasPaths = getVariadicArgument(INPUT_HINT);
        final List<File> atlasResourceList = new ArrayList<>();

        inputAtlasPaths.stream().forEach(path ->
        {
            final File file = new File(path);
            if (!file.exists())
            {
                printlnWarnMessage("file not found: " + path);
            }
            else
            {
                printlnStdout("Loading " + path);
                atlasResourceList.add(file);
            }
        });

        if (hasOption(STRICT_OPTION_LONG))
        {
            if (atlasResourceList.size() != inputAtlasPaths.size())
            {
                printlnErrorMessage("terminating due to missing atlas");
                return 1;
            }
        }

        if (atlasResourceList.isEmpty())
        {
            printlnErrorMessage("no valid input atlases found");
            return 1;
        }

        final Path outputParentPath = Paths
                .get(getOptionArgument(OUTPUT_DIRECTORY_OPTION_LONG).orElse(""));

        if (!outputParentPath.toFile().exists())
        {
            try
            {
                new File(outputParentPath.toAbsolutePath().toString()).mkdirs();
            }
            catch (final Exception exception)
            {
                printlnErrorMessage("failed to create output directory "
                        + outputParentPath.toAbsolutePath().toString());
                return 1;
            }
        }

        atlasResourceList.stream().forEach(resource ->
        {
            printlnStdout("Converting " + resource.getFile().getAbsolutePath() + "...");
            final PackedAtlas output = new PackedAtlasCloner()
                    .cloneFrom(new AtlasResourceLoader().load(resource));
            try
            {
                if (hasOption(GEOJSON_OPTION_LONG))
                {
                    final Path filePath = Paths
                            .get(resource.getFile().getName() + FileSuffix.GEO_JSON);
                    final Path concatenatedPath = Paths.get(
                            outputParentPath.toAbsolutePath().toString(),
                            filePath.getFileName().toString());
                    final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
                    output.saveAsGeoJson(outputFile);
                    printlnStdout("Saved to " + outputFile.getFile().getAbsolutePath());

                }
                else if (hasOption(LDGEOJSON_OPTION_LONG))
                {
                    final Path filePath = Paths
                            .get(resource.getFile().getName() + FileSuffix.GEO_JSON);
                    final Path concatenatedPath = Paths.get(
                            outputParentPath.toAbsolutePath().toString(),
                            filePath.getFileName().toString());
                    final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
                    output.saveAsLineDelimitedGeoJsonFeatures(outputFile, (entity, json) ->
                    {
                        // Dummy consumer, we don't need to mutate the JSON
                    });
                    printlnStdout("Saved to " + outputFile.getFile().getAbsolutePath());
                }
                else
                {
                    final Path filePath = Paths.get(resource.getFile().getName() + FileSuffix.TEXT);
                    final Path concatenatedPath = Paths.get(
                            outputParentPath.toAbsolutePath().toString(),
                            filePath.getFileName().toString());
                    final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
                    output.saveAsText(outputFile);
                    printlnStdout("Saved to " + outputFile.getFile().getAbsolutePath());
                }
            }
            catch (final Exception exception)
            {
                printlnErrorMessage("failed to save text file for " + resource.getFile().getName()
                        + ": " + exception.getMessage());
            }
        });

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "packed-to-text";
    }

    @Override
    public String getSimpleDescription()
    {
        return "Transform a PackedAtlas into a human-readable format";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION",
                PackedToTextAtlasCommand.class.getResourceAsStream(DESCRIPTION_SECTION));
        addManualPageSection("EXAMPLES",
                PackedToTextAtlasCommand.class.getResourceAsStream(EXAMPLES_SECTION));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        setVersion(VERSION);
        registerOption(STRICT_OPTION_LONG, STRING_OPTION_SHORT, STRICT_OPTION_DESCRIPTION);
        registerOption(GEOJSON_OPTION_LONG, GEOJSON_OPTION_SHORT, GEOJSON_OPTION_DESCRIPTION);
        registerOption(LDGEOJSON_OPTION_LONG, LDGEOJSON_OPTION_SHORT, LDGEOJSON_OPTION_DESCRIPTION);
        registerOptionWithRequiredArgument(OUTPUT_DIRECTORY_OPTION_LONG,
                OUTPUT_DIRECTORY_OPTION_SHORT, OUTPUT_DIRECTORY_OPTION_DESCRIPTION,
                OUTPUT_DIRECTORY_OPTION_HINT);
        registerArgument(INPUT_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
    }
}

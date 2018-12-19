package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentFetcher;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.VariadicAtlasLoaderCommand;

/**
 * @author lcram
 */
public class PackedToTextAtlasCommand extends VariadicAtlasLoaderCommand
{
    private static final String DESCRIPTION_SECTION = "PackedToTextAtlasCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "PackedToTextAtlasCommandExamplesSection.txt";

    private static final String GEOJSON_OPTION_LONG = "geojson";
    private static final Character GEOJSON_OPTION_SHORT = 'g';
    private static final String GEOJSON_OPTION_DESCRIPTION = "Save atlas as GeoJSON.";

    private static final String LDGEOJSON_OPTION_LONG = "ldgeojson";
    private static final Character LDGEOJSON_OPTION_SHORT = 'l';
    private static final String LDGEOJSON_OPTION_DESCRIPTION = "Save atlas as line-delimited GeoJSON.";

    private static final String PARALLEL_OPTION_LONG = "parallel";
    private static final Character PARALLEL_OPTION_SHORT = 'p';
    private static final String PARALLEL_OPTION_DESCRIPTION = "Process the atlases in parallel.";

    private static final Integer DEFAULT_AND_GEOJSON_CONTEXT = 3;
    private static final Integer LDGEOJSON_CONTEXT = 4;

    private final OptionAndArgumentFetcher fetcher;
    private final CommandOutputDelegate output;

    public static void main(final String[] args)
    {
        new PackedToTextAtlasCommand().runSubcommandAndExit(args);
    }

    public PackedToTextAtlasCommand()
    {
        super();
        this.fetcher = this.getOptionAndArgumentFetcher();
        this.output = this.getCommandOutputDelegate();
    }

    @Override
    public int execute() // NOSONAR
    {
        final List<File> atlasResourceList = this.getInputAtlasResources();
        if (atlasResourceList.isEmpty())
        {
            this.output.printlnErrorMessage("no input atlases");
            return 1;
        }
        final Stream<File> atlasResourceStream = atlasResourceList.stream();

        final Optional<Path> outputParentPath = this.getOutputPath();
        if (!outputParentPath.isPresent())
        {
            this.output.printlnErrorMessage("invalid output path");
            return 1;
        }

        if (this.fetcher.hasOption(PARALLEL_OPTION_LONG))
        {
            atlasResourceStream.parallel();
        }

        atlasResourceStream.forEach(resource ->
        {
            this.output.printlnStdout("Converting " + resource.getFile().getAbsolutePath() + "...");
            final PackedAtlas outputAtlas = new PackedAtlasCloner()
                    .cloneFrom(new AtlasResourceLoader().load(resource));
            try
            {
                writeOutput(resource, outputParentPath, outputAtlas);
            }
            catch (final Exception exception)
            {
                this.output.printlnErrorMessage("failed to save text file for "
                        + resource.getFile().getName() + ": " + exception.getMessage());
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
        super.registerManualPageSections();
        addManualPageSection("DESCRIPTION",
                PackedToTextAtlasCommand.class.getResourceAsStream(DESCRIPTION_SECTION));
        addManualPageSection("EXAMPLES",
                PackedToTextAtlasCommand.class.getResourceAsStream(EXAMPLES_SECTION));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOption(GEOJSON_OPTION_LONG, GEOJSON_OPTION_SHORT, GEOJSON_OPTION_DESCRIPTION,
                DEFAULT_AND_GEOJSON_CONTEXT);
        registerOption(LDGEOJSON_OPTION_LONG, LDGEOJSON_OPTION_SHORT, LDGEOJSON_OPTION_DESCRIPTION,
                LDGEOJSON_CONTEXT);
        registerOption(PARALLEL_OPTION_LONG, PARALLEL_OPTION_SHORT, PARALLEL_OPTION_DESCRIPTION,
                DEFAULT_AND_GEOJSON_CONTEXT, LDGEOJSON_CONTEXT);
        super.registerOptionsAndArguments();
    }

    private void writeOutput(final File resource, final Optional<Path> outputParentPath,
            final PackedAtlas outputAtlas)
    {
        if (!outputParentPath.isPresent())
        {
            return;
        }

        if (this.fetcher.getParserContext() == DEFAULT_AND_GEOJSON_CONTEXT)
        {
            if (this.fetcher.hasOption(GEOJSON_OPTION_LONG))
            {
                final Path filePath = Paths.get(resource.getFile().getName() + FileSuffix.GEO_JSON);
                final Path concatenatedPath = Paths.get(
                        outputParentPath.get().toAbsolutePath().toString(),
                        filePath.getFileName().toString());
                final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
                outputAtlas.saveAsGeoJson(outputFile);
                this.output.printlnStdout("Saved to " + outputFile.getFile().getAbsolutePath()); // NOSONAR

            }
            else
            {
                final Path filePath = Paths.get(resource.getFile().getName() + FileSuffix.TEXT);
                final Path concatenatedPath = Paths.get(
                        outputParentPath.get().toAbsolutePath().toString(),
                        filePath.getFileName().toString());
                final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
                outputAtlas.saveAsText(outputFile);
                this.output.printlnStdout("Saved to " + outputFile.getFile().getAbsolutePath());
            }
        }
        else if (this.fetcher.getParserContext() == LDGEOJSON_CONTEXT
                && this.fetcher.hasOption(LDGEOJSON_OPTION_LONG))
        {
            final Path filePath = Paths.get(resource.getFile().getName() + FileSuffix.GEO_JSON);
            final Path concatenatedPath = Paths.get(
                    outputParentPath.get().toAbsolutePath().toString(),
                    filePath.getFileName().toString());
            final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
            outputAtlas.saveAsLineDelimitedGeoJsonFeatures(outputFile, (entity, json) ->
            {
                // Dummy consumer, we don't need to mutate the JSON
            });
            this.output.printlnStdout("Saved to " + outputFile.getFile().getAbsolutePath());
        }
    }
}

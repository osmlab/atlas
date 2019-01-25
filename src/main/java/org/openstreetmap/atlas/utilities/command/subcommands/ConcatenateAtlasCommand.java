package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.VariadicAtlasLoaderCommand;

/**
 * @author lcram
 */
public class ConcatenateAtlasCommand extends VariadicAtlasLoaderCommand
{
    private static final String OUTPUT_ATLAS = "output.atlas";

    private static final String DESCRIPTION_SECTION = "ConcatenateAtlasCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "ConcatenateAtlasCommandExamplesSection.txt";

    private final OptionAndArgumentDelegate optargDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new ConcatenateAtlasCommand().runSubcommandAndExit(args);
    }

    public ConcatenateAtlasCommand()
    {
        super();
        this.optargDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final List<File> atlasResourceList = this.getInputAtlasResources();
        if (atlasResourceList.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("no input atlases");
            return 1;
        }

        final Optional<Path> outputParentPath = this.getOutputPath();
        if (!outputParentPath.isPresent())
        {
            this.outputDelegate.printlnErrorMessage("invalid output path");
            return 1;
        }

        if (this.optargDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnStdout("Cloning...");
        }
        final PackedAtlas outputAtlas = new PackedAtlasCloner()
                .cloneFrom(new AtlasResourceLoader().load(atlasResourceList));
        final Path concatenatedPath = Paths.get(outputParentPath.get().toAbsolutePath().toString(),
                OUTPUT_ATLAS);
        final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
        outputAtlas.save(outputFile);

        if (this.optargDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnStdout("Saved to " + concatenatedPath.toString());
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "fatlas";
    }

    @Override
    public String getSimpleDescription()
    {
        return "create and save a fatlas using the MultiAtlas";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION",
                ConcatenateAtlasCommand.class.getResourceAsStream(DESCRIPTION_SECTION));
        addManualPageSection("EXAMPLES",
                ConcatenateAtlasCommand.class.getResourceAsStream(EXAMPLES_SECTION));
        super.registerManualPageSections();
    }
}

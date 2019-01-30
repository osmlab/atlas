package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;

/**
 * @author lcram
 */
public class ConcatenateAtlasCommand extends AtlasLoaderCommand
{
    private static final String OUTPUT_ATLAS = "output.atlas";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    private final List<Atlas> atlases = new ArrayList<>();
    private Optional<Path> outputParentPath;

    public static void main(final String[] args)
    {
        new ConcatenateAtlasCommand().runSubcommandAndExit(args);
    }

    public ConcatenateAtlasCommand()
    {
        super();
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
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
        addManualPageSection("DESCRIPTION", ConcatenateAtlasCommand.class
                .getResourceAsStream("ConcatenateAtlasCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", ConcatenateAtlasCommand.class
                .getResourceAsStream("ConcatenateAtlasCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    protected int finish()
    {
        if (this.atlases.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("could not load atlas(es)");
            return 1;
        }

        final Atlas atlas = new MultiAtlas(this.atlases);

        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("cloning...");
        }
        final PackedAtlas outputAtlas = new PackedAtlasCloner().cloneFrom(atlas);
        final Path concatenatedPath = Paths.get(this.outputParentPath
                .orElseThrow(AtlasShellToolsException::new).toAbsolutePath().toString(),
                OUTPUT_ATLAS);
        final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
        outputAtlas.save(outputFile);

        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("saved to " + concatenatedPath.toString());
        }

        return 0;
    }

    @Override
    protected void processAtlas(final Atlas atlas)
    {
        this.atlases.add(atlas);
    }

    @Override
    protected int start()
    {
        this.outputParentPath = this.getOutputPath();
        if (!this.outputParentPath.isPresent())
        {
            this.outputDelegate.printlnErrorMessage("invalid output path");
            return 1;
        }
        return 0;
    }
}

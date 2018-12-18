package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentFetcher;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;

/**
 * @author lcram
 */
public class ConcatenateAtlasCommand extends AbstractAtlasShellToolsCommand
{
    private static final String INPUT_HINT = "input";
    private static final String OUTPUT_HINT = "output";

    private static final String STRICT_OPTION_LONG = "strict";
    private static final Character STRING_OPTION_SHORT = 's';
    private static final String STRICT_OPTION_DESCRIPTION = "Fail fast if any input atlases are missing.";

    private static final String DESCRIPTION_SECTION = "ConcatenateAtlasCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "ConcatenateAtlasCommandExamplesSection.txt";

    private final OptionAndArgumentFetcher fetcher;
    private final CommandOutputDelegate output;

    public static void main(final String[] args)
    {
        new ConcatenateAtlasCommand().runSubcommandAndExit(args);
    }

    public ConcatenateAtlasCommand()
    {
        this.fetcher = this.getOptionAndArgumentFetcher();
        this.output = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final List<String> inputAtlasPaths = this.fetcher.getVariadicArgument(INPUT_HINT);
        final String outputAtlasPath = this.fetcher.getUnaryArgument(OUTPUT_HINT).get();
        final List<File> atlasResourceList = new ArrayList<>();

        inputAtlasPaths.stream().forEach(path ->
        {
            final File file = new File(path);
            if (!file.exists())
            {
                this.output.printlnWarnMessage("file not found: " + path);
            }
            else
            {
                this.output.printlnStdout("Loading " + path);
                atlasResourceList.add(file);
            }
        });

        if (this.fetcher.hasOption(STRICT_OPTION_LONG))
        {
            if (atlasResourceList.size() != inputAtlasPaths.size()) // NOSONAR
            {
                this.output.printlnErrorMessage("terminating due to missing atlas");
                return 1;
            }
        }

        if (atlasResourceList.isEmpty())
        {
            this.output.printlnErrorMessage("no valid input atlases found");
            return 1;
        }

        this.output.printlnStdout("Cloning...");
        final PackedAtlas outputAtlas = new PackedAtlasCloner()
                .cloneFrom(new AtlasResourceLoader().load(atlasResourceList));
        final File outputFile = new File(outputAtlasPath);
        outputAtlas.save(outputFile);

        this.output.printlnStdout("Saved to " + outputAtlasPath);

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
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOption(STRICT_OPTION_LONG, STRING_OPTION_SHORT, STRICT_OPTION_DESCRIPTION);
        registerArgument(INPUT_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        registerArgument(OUTPUT_HINT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
    }
}

package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.AbstractOSMSubcommand;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;

/**
 * @author lcram
 */
public class ConcatenateAtlasSubcommand extends AbstractOSMSubcommand
{
    private static final String INPUT_HINT = "input";
    private static final String OUTPUT_HINT = "output";

    private static final String STRICT_OPTION_LONG = "strict";
    private static final Character STRING_OPTION_SHORT = 's';
    private static final String STRICT_OPTION_DESCRIPTION = "Fail fast if any input atlases are missing.";

    private static final String VERSION = "1.0.0";

    public static void main(final String[] args)
    {
        new ConcatenateAtlasSubcommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        final List<String> inputAtlasPaths = getVariadicArgument(INPUT_HINT);
        final String outputAtlasPath = getUnaryArgument(OUTPUT_HINT).get();
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
                printVerboseStdout("Loading " + path + "\n");
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

        printVerboseStdout("Cloning...\n");
        final PackedAtlas output = new PackedAtlasCloner()
                .cloneFrom(new AtlasResourceLoader().load(atlasResourceList));
        final File outputFile = new File(outputAtlasPath);
        output.save(outputFile);

        printVerboseStdout("Saved to " + outputAtlasPath + "\n");

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
    public void registerOptionsAndArguments()
    {
        setVersion(VERSION);
        registerOption(STRICT_OPTION_LONG, STRING_OPTION_SHORT, STRICT_OPTION_DESCRIPTION);
        registerArgument(INPUT_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        registerArgument(OUTPUT_HINT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
    }

}

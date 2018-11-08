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
    private static final String VERBOSE_LONG = "verbose";

    private static final String VERSION = "1.0.0";

    public static void main(final String[] args)
    {
        new ConcatenateAtlasSubcommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        final List<String> inputAtlasPaths = getVariadicArgument(INPUT_HINT);
        final String outputAtlasPath = getUnaryArgument(OUTPUT_HINT);
        final List<File> atlasResourceList = new ArrayList<>();

        inputAtlasPaths.stream().forEach(path ->
        {
            final File file = new File(path);
            if (!file.exists())
            {
                System.err.println("File not found: " + path);
                System.exit(1);
            }
            atlasResourceList.add(file);
            if (hasOption(VERBOSE_LONG))
            {
                System.out.println("Loading " + path);
            }
        });

        final AtlasResourceLoader loader = new AtlasResourceLoader();
        if (hasOption(VERBOSE_LONG))
        {
            System.out.println("Cloning...");
        }
        final PackedAtlas output = new PackedAtlasCloner()
                .cloneFrom(loader.load(atlasResourceList));
        final File outputFile = new File(outputAtlasPath);
        output.save(outputFile);

        if (hasOption(VERBOSE_LONG))
        {
            System.out.println("Saved to " + outputAtlasPath);
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "catlas";
    }

    @Override
    public String getSimpleDescription()
    {
        return "concatenate atlases using MultiAtlas";
    }

    @Override
    public void registerOptionsAndArguments()
    {
        setVersion(VERSION);
        registerArgument(INPUT_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        registerArgument(OUTPUT_HINT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
    }

}

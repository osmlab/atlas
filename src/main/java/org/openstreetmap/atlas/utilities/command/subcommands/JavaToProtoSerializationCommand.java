package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.VariadicAtlasLoaderCommand;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class JavaToProtoSerializationCommand extends VariadicAtlasLoaderCommand
{
    private static final String CHECK_OPTION_LONG = "check";
    private static final Character CHECK_OPTION_SHORT = 'c';
    private static final String CHECK_OPTION_DESCRIPTION = "Check the serialization format of the atlas without converting.";

    private static final String REVERSE_OPTION_LONG = "reverse";
    private static final Character REVERSE_OPTION_SHORT = 'R';
    private static final String REVERSE_OPTION_DESCRIPTION = "Convert a Protocol Buffers atlas back to Java serialization.";

    private static final Integer CHECK_CONTEXT = 4;

    private final OptionAndArgumentDelegate optargDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new JavaToProtoSerializationCommand().runSubcommandAndExit(args);
    }

    public JavaToProtoSerializationCommand()
    {
        super();
        this.optargDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final List<File> atlasResourceList = this.getInputAtlasResources();
        final List<String> atlasNames = this.getFileNames(atlasResourceList);
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

        int index = 0;
        for (final File atlasResource : atlasResourceList)
        {
            final PackedAtlas outputAtlas = (PackedAtlas) new AtlasResourceLoader()
                    .load(atlasResource);
            if (this.optargDelegate.getParserContext() == CHECK_CONTEXT)
            {
                this.outputDelegate.printStdout("atlas ");
                this.outputDelegate.printStdout(atlasResource.getAbsolutePath(), TTYAttribute.BOLD);
                this.outputDelegate.printStdout(" format: ");
                this.outputDelegate.printlnStdout(outputAtlas.getSerializationFormat().toString(),
                        TTYAttribute.BOLD);
            }
            else
            {
                if (this.optargDelegate.hasOption(REVERSE_OPTION_LONG))
                {
                    outputAtlas.setSaveSerializationFormat(AtlasSerializationFormat.JAVA);
                }
                else
                {
                    outputAtlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
                }
                final Path concatenatedPath = Paths.get(
                        outputParentPath.get().toAbsolutePath().toString(), atlasNames.get(index));
                final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
                outputAtlas.save(outputFile);

                if (this.optargDelegate.hasVerboseOption())
                {
                    this.outputDelegate.printlnStdout("Saved to " + concatenatedPath.toString());
                }
            }

            index++;
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "java2proto";
    }

    @Override
    public String getSimpleDescription()
    {
        return "convert Java-serialized atlases to Protocol Buffers format";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", JavaToProtoSerializationCommand.class
                .getResourceAsStream("JavaToProtoSerializationCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", JavaToProtoSerializationCommand.class
                .getResourceAsStream("JavaToProtoSerializationCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOption(REVERSE_OPTION_LONG, REVERSE_OPTION_SHORT, REVERSE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL);
        registerOption(CHECK_OPTION_LONG, CHECK_OPTION_SHORT, CHECK_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, CHECK_CONTEXT);
        super.registerOptionsAndArguments();
    }
}

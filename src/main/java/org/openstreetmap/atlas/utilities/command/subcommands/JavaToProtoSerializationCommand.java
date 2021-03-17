package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderTemplate;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.OutputDirectoryTemplate;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class JavaToProtoSerializationCommand extends AbstractAtlasShellToolsCommand
{
    private static final String CHECK_OPTION_LONG = "check";
    private static final Character CHECK_OPTION_SHORT = 'c';
    private static final String CHECK_OPTION_DESCRIPTION = "Check the serialization format of the atlas(es) without converting.";

    private static final String REVERSE_OPTION_LONG = "reverse";
    private static final Character REVERSE_OPTION_SHORT = 'R';
    private static final String REVERSE_OPTION_DESCRIPTION = "Convert Protocol Buffers atlas(es) back to Java serialization.";

    private static final Integer CHECK_CONTEXT = 4;

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new JavaToProtoSerializationCommand().runSubcommandAndExit(args);
    }

    public JavaToProtoSerializationCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        return AtlasLoaderTemplate.execute(this, null, this::processAtlas, null);
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
        registerManualPageSectionsFromTemplate(new AtlasLoaderTemplate());
        registerManualPageSectionsFromTemplate(new OutputDirectoryTemplate());
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOption(REVERSE_OPTION_LONG, REVERSE_OPTION_SHORT, REVERSE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL);
        registerOption(CHECK_OPTION_LONG, CHECK_OPTION_SHORT, CHECK_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, CHECK_CONTEXT);
        registerOptionsAndArgumentsFromTemplate(new AtlasLoaderTemplate(
                AbstractAtlasShellToolsCommand.DEFAULT_CONTEXT, CHECK_CONTEXT));
        registerOptionsAndArgumentsFromTemplate(
                new OutputDirectoryTemplate(AbstractAtlasShellToolsCommand.DEFAULT_CONTEXT));
        super.registerOptionsAndArguments();
    }

    private void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
        PackedAtlas outputAtlas;
        try
        {
            outputAtlas = (PackedAtlas) atlas;
        }
        catch (final ClassCastException exception)
        {
            outputAtlas = new PackedAtlasCloner().cloneFrom(atlas);
        }

        if (this.optionAndArgumentDelegate.getParserContext() == CHECK_CONTEXT)
        {
            this.outputDelegate.printStdout("atlas ");
            this.outputDelegate.printStdout(atlasResource.getPathString(), TTYAttribute.BOLD);
            this.outputDelegate.printStdout(" format: ");
            this.outputDelegate.printlnStdout(outputAtlas.getSerializationFormat().toString(),
                    TTYAttribute.BOLD);
        }
        else
        {
            if (this.optionAndArgumentDelegate.hasOption(REVERSE_OPTION_LONG))
            {
                outputAtlas.setSaveSerializationFormat(AtlasSerializationFormat.JAVA);
            }
            else
            {
                outputAtlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
            }

            final Optional<Path> outputPathOptional = OutputDirectoryTemplate.getOutputPath(this);
            if (outputPathOptional.isEmpty())
            {
                this.outputDelegate
                        .printlnWarnMessage("could not save " + atlasFileName + ", skipping...");
                return;
            }
            final Path concatenatedPath = outputPathOptional
                    .map(path -> Paths.get(path.toAbsolutePath().toString(), atlasFileName))
                    .orElseThrow(AtlasShellToolsException::new);

            final File outputFile = new File(concatenatedPath.toAbsolutePath().toString(),
                    this.getFileSystem());
            outputAtlas.save(outputFile);

            if (this.optionAndArgumentDelegate.hasVerboseOption())
            {
                this.outputDelegate.printlnStdout("Saved to " + concatenatedPath.toString());
            }
        }
    }
}

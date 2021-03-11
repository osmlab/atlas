package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * A {@link MultipleOutputCommand} is any command which may produce multiple output files, and wants
 * to provide users with a way to specify the desired location of those files. This command template
 * registers a '--output' option, the argument to which will be used as an output directory.
 *
 * @author lcram
 * @deprecated
 */
@Deprecated
public abstract class MultipleOutputCommand extends AbstractAtlasShellToolsCommand
{
    public static final String OUTPUT_DIRECTORY_OPTION_LONG = "output";
    private static final Character OUTPUT_DIRECTORY_OPTION_SHORT = 'o';
    private static final String OUTPUT_DIRECTORY_OPTION_DESCRIPTION = "Specify an alternate output directory for any output files. If the directory "
            + "does not exist, it will be created.";
    private static final String OUTPUT_DIRECTORY_OPTION_HINT = "dir";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;
    private Path outputPath;

    public MultipleOutputCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
        this.outputPath = null;
    }

    /**
     * Populate the output path field. Subclasses should override this method, but invoke it with
     * super.execute to populate the outputPath field. The subclass can check the return code of
     * this method to see if the output path was parsed successfully.
     *
     * @return the exit status, 0 indicates success while 1 indicates that the output path was
     *         invalid
     */
    @Override
    public int execute()
    {
        final Optional<Path> outputPathOptional = parseOutputPath();
        if (outputPathOptional.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("invalid output path");
            return 1;
        }
        else
        {
            this.outputPath = outputPathOptional.get();
        }
        return 0;
    }

    public Path getOutputPath()
    {
        return this.outputPath;
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("MULTIPLE OUTPUT", MultipleOutputCommand.class
                .getResourceAsStream("MultipleOutputCommandSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        final Integer[] contexts = this.optionAndArgumentDelegate.getFilteredRegisteredContexts()
                .toArray(new Integer[0]);
        registerOptionWithRequiredArgument(OUTPUT_DIRECTORY_OPTION_LONG,
                OUTPUT_DIRECTORY_OPTION_SHORT, OUTPUT_DIRECTORY_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, OUTPUT_DIRECTORY_OPTION_HINT, contexts);
        super.registerOptionsAndArguments();
    }

    private Optional<Path> parseOutputPath()
    {
        final Path outputParentPath = this.getFileSystem().getPath(this.optionAndArgumentDelegate
                .getOptionArgument(OUTPUT_DIRECTORY_OPTION_LONG).orElse(""));

        // If output path already exists and is a file, then fail
        if (Files.isRegularFile(outputParentPath))
        {
            this.outputDelegate.printlnErrorMessage(
                    outputParentPath.toString() + " already exists and is a file");
            return Optional.empty();
        }

        // If output path does not exist, create it using 'mkdir -p' behaviour
        if (!Files.exists(outputParentPath))
        {
            try
            {
                new File(outputParentPath.toAbsolutePath().toString(), this.getFileSystem())
                        .mkdirs();
            }
            catch (final Exception exception)
            {
                this.outputDelegate.printlnErrorMessage(
                        "failed to create output directory " + outputParentPath.toString());
                return Optional.empty();
            }
        }

        // If output path is not writable, fail
        if (!Files.isWritable(outputParentPath))
        {
            this.outputDelegate
                    .printlnErrorMessage(outputParentPath.toString() + " is not writable");
            return Optional.empty();
        }

        return Optional.of(outputParentPath);
    }
}

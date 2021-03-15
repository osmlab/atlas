package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AtlasShellToolsCommandTemplate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OutputDirectoryTemplate} provides a template for any command which may produce
 * multiple output files and wants to provide users with a way to specify the desired location of
 * those files. This command template registers an '--output-directory' option, the argument to
 * which will be possibly created and used as an output directory.
 *
 * @author lcram
 */
public class OutputDirectoryTemplate implements AtlasShellToolsCommandTemplate
{
    private static final Logger logger = LoggerFactory.getLogger(OutputDirectoryTemplate.class);

    private static final String OUTPUT_DIRECTORY_OPTION_LONG = "output-directory";
    private static final Character OUTPUT_DIRECTORY_OPTION_SHORT = 'o';

    private final Integer[] contexts;

    /**
     * Get the output path specified by the user. If the returned {@link Optional} is empty, then
     * the output path could not be parsed and it is recommended that you exit with an error.
     * 
     * @param parentCommand
     *            the parent command that controls this template
     * @return an {@link Optional} containing the output path for this command
     */
    public static Optional<Path> getOutputPath(final AbstractAtlasShellToolsCommand parentCommand)
    {
        /*
         * Grab output path from --output-directory option if present. If that option is not
         * present, return the current working directory.
         */
        final Path outputParentPath = parentCommand.getFileSystem()
                .getPath(parentCommand.getOptionAndArgumentDelegate()
                        .getOptionArgument(OUTPUT_DIRECTORY_OPTION_LONG).orElse(""));

        // If output path already exists and is a file, then fail
        if (Files.isRegularFile(outputParentPath))
        {
            parentCommand.getCommandOutputDelegate().printlnErrorMessage(
                    outputParentPath.toString() + " already exists and is a file");
            return Optional.empty();
        }

        // If output path does not exist, create it using 'mkdir -p' behaviour
        if (!Files.exists(outputParentPath))
        {
            try
            {
                new File(outputParentPath.toAbsolutePath().toString(),
                        parentCommand.getFileSystem()).mkdirs();
            }
            catch (final Exception exception)
            {
                parentCommand.getCommandOutputDelegate().printlnErrorMessage(
                        "failed to create output directory " + outputParentPath.toString());
                logger.error("Failed to create output directory", exception);
                return Optional.empty();
            }
        }

        // If output path is not writable, fail
        if (!Files.isWritable(outputParentPath))
        {
            parentCommand.getCommandOutputDelegate()
                    .printlnErrorMessage(outputParentPath.toString() + " is not writable");
            return Optional.empty();
        }

        return Optional.of(outputParentPath);
    }

    /**
     * This constructor allows callers to specify under which contexts they want the options
     * provided by this template to appear. If left blank, this template will only be applied to the
     * default context.
     *
     * @param contexts
     *            the parse contexts under which you want the options provided by this template to
     *            appear
     */
    public OutputDirectoryTemplate(final Integer... contexts)
    {
        this.contexts = contexts;
    }

    @Override
    public void registerManualPageSections(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.addManualPageSection("OUTPUT DIRECTORY", OutputDirectoryTemplate.class
                .getResourceAsStream("OutputDirectoryTemplateSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.registerOptionWithRequiredArgument(OUTPUT_DIRECTORY_OPTION_LONG,
                OUTPUT_DIRECTORY_OPTION_SHORT,
                "Specify an alternate output directory for any output files. If the directory "
                        + "does not exist, it will be created.",
                OptionOptionality.OPTIONAL, "dir", this.contexts);
    }
}

package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * A helper super class for any command that wants to load atlas files from disk. Provides a builtin
 * variadic input argument, as well as automatic conversion from paths to resources with a
 * '--strict' option to fail fast. Also provides an '--output' flag, which allows users to specify
 * an alternate directory for any output.
 *
 * @author lcram
 */
public abstract class VariadicAtlasLoaderCommand extends AbstractAtlasShellToolsCommand
{
    private static final String INPUT_HINT = "input-atlases";

    private static final String LOADER_SECTION = "AtlasLoaderCommandLoaderSection.txt";

    private static final String STRICT_OPTION_LONG = "strict";
    private static final String STRICT_OPTION_DESCRIPTION = "Fail fast if any input atlases are missing.";

    private static final String OUTPUT_DIRECTORY_OPTION_LONG = "output";
    private static final Character OUTPUT_DIRECTORY_OPTION_SHORT = 'o';
    private static final String OUTPUT_DIRECTORY_OPTION_DESCRIPTION = "Specify an alternate output directory for any output files. If the directory "
            + "does not exist, it will be created.";
    private static final String OUTPUT_DIRECTORY_OPTION_HINT = "dir";

    private final OptionAndArgumentDelegate optargDelegate;
    private final CommandOutputDelegate outputDelegate;

    public VariadicAtlasLoaderCommand()
    {
        this.optargDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    public List<String> getFileNames(final List<File> atlasResources)
    {
        return atlasResources.stream().map(File::getName).collect(Collectors.toList());
    }

    public List<File> getInputAtlasResources()
    {
        final List<String> inputAtlasPaths = this.optargDelegate.getVariadicArgument(INPUT_HINT);
        final List<File> atlasResourceList = new ArrayList<>();

        inputAtlasPaths.stream().forEach(path ->
        {
            final File file = new File(path);
            if (!file.exists())
            {
                this.outputDelegate.printlnWarnMessage("file not found: " + path);
            }
            else
            {
                if (this.optargDelegate.hasVerboseOption())
                {
                    this.outputDelegate.printlnStdout("Loading " + path);
                }
                atlasResourceList.add(file);
            }
        });

        if (this.optargDelegate.hasOption(STRICT_OPTION_LONG)
                && atlasResourceList.size() != inputAtlasPaths.size())
        {
            this.outputDelegate.printlnErrorMessage("terminating due to missing atlas");
            return new ArrayList<>();
        }

        if (atlasResourceList.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("no valid input atlases found");
            return new ArrayList<>();
        }

        return atlasResourceList;
    }

    public Optional<Path> getOutputPath()
    {
        final Path outputParentPath = Paths.get(
                this.optargDelegate.getOptionArgument(OUTPUT_DIRECTORY_OPTION_LONG).orElse(""));

        // If output path already exists and is a file, then fail
        if (outputParentPath.toAbsolutePath().toFile().isFile())
        {
            this.outputDelegate.printlnErrorMessage(
                    outputParentPath.toString() + " already exists and is a file");
            return Optional.empty();
        }

        // If output path does not exist, create it using 'mkdir -p' behaviour
        if (!outputParentPath.toAbsolutePath().toFile().exists())
        {
            try
            {
                new File(outputParentPath.toAbsolutePath().toString()).mkdirs();
            }
            catch (final Exception exception)
            {
                this.outputDelegate.printlnErrorMessage(
                        "failed to create output directory " + outputParentPath.toString());
                return Optional.empty();
            }
        }

        // If output path is not writable, fail
        if (!outputParentPath.toAbsolutePath().toFile().canWrite())
        {
            this.outputDelegate
                    .printlnErrorMessage(outputParentPath.toString() + " is not writable");
            return Optional.empty();
        }

        return Optional.ofNullable(outputParentPath);
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("ATLAS LOADER",
                VariadicAtlasLoaderCommand.class.getResourceAsStream(LOADER_SECTION));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        final Integer[] contexts = this.optargDelegate.getFilteredRegisteredContexts()
                .toArray(new Integer[0]);
        registerOption(STRICT_OPTION_LONG, STRICT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                contexts);
        registerOptionWithRequiredArgument(OUTPUT_DIRECTORY_OPTION_LONG,
                OUTPUT_DIRECTORY_OPTION_SHORT, OUTPUT_DIRECTORY_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, OUTPUT_DIRECTORY_OPTION_HINT, contexts);
        registerArgument(INPUT_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED,
                contexts);
        super.registerOptionsAndArguments();
    }
}

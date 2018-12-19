package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentFetcher;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;

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
    private static final Character STRING_OPTION_SHORT = 's';
    private static final String STRICT_OPTION_DESCRIPTION = "Fail fast if any input atlases are missing.";

    private static final String OUTPUT_DIRECTORY_OPTION_LONG = "output";
    private static final Character OUTPUT_DIRECTORY_OPTION_SHORT = 'o';
    private static final String OUTPUT_DIRECTORY_OPTION_DESCRIPTION = "Specify an alternate output directory for any output files. If the directory\n"
            + "does not exist, it will be created.";
    private static final String OUTPUT_DIRECTORY_OPTION_HINT = "dir";

    private final OptionAndArgumentFetcher fetcher;
    private final CommandOutputDelegate output;

    public VariadicAtlasLoaderCommand()
    {
        this.fetcher = this.getOptionAndArgumentFetcher();
        this.output = this.getCommandOutputDelegate();
    }

    public List<File> getInputAtlasResources()
    {
        final List<String> inputAtlasPaths = this.fetcher.getVariadicArgument(INPUT_HINT);
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
                return new ArrayList<>();
            }
        }

        if (atlasResourceList.isEmpty())
        {
            this.output.printlnErrorMessage("no valid input atlases found");
            return new ArrayList<>();
        }

        return atlasResourceList;
    }

    public Optional<Path> getOutputPath()
    {
        final Path outputParentPath = Paths
                .get(this.fetcher.getOptionArgument(OUTPUT_DIRECTORY_OPTION_LONG).orElse(""));

        if (!outputParentPath.toAbsolutePath().toFile().exists())
        {
            try
            {
                new File(outputParentPath.toAbsolutePath().toString()).mkdirs();
            }
            catch (final Exception exception)
            {
                this.output.printlnErrorMessage(
                        "failed to create output directory " + outputParentPath.toString());
                return Optional.empty();
            }
        }

        if (!outputParentPath.toAbsolutePath().toFile().canWrite())
        {
            this.output.printlnErrorMessage(outputParentPath.toString() + " is not writable");
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
        registerOption(STRICT_OPTION_LONG, STRING_OPTION_SHORT, STRICT_OPTION_DESCRIPTION);
        registerOptionWithRequiredArgument(OUTPUT_DIRECTORY_OPTION_LONG,
                OUTPUT_DIRECTORY_OPTION_SHORT, OUTPUT_DIRECTORY_OPTION_DESCRIPTION,
                OUTPUT_DIRECTORY_OPTION_HINT);
        registerArgument(INPUT_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
    }
}

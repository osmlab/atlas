package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentFetcher;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;

/**
 * A helper super class for any command that wants to load atlas files from disk. Provides a builtin
 * variadic input argument, as well as automatic conversion from paths to resources with a
 * '--strict' option to fail fast.
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
                // TODO probably should do something better here
                throw new CoreException(
                        "Using strict mode and some input atlases could not be found");
            }
        }

        if (atlasResourceList.isEmpty())
        {
            this.output.printlnErrorMessage("no valid input atlases found");
            // TODO probably should do something better here
            throw new CoreException("None of the provided atlases could be found");
        }

        return atlasResourceList;
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
        registerArgument(INPUT_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
    }
}

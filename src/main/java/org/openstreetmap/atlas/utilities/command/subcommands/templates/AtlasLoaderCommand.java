package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.command.AbstractAtlasSubCommand;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
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
 * an alternate directory for any output.<br>
 * Subclasses can override the start(), handle(), and finish() methods, which provide a way to
 * operate on the input atlases without having to deal with resource loading and iterating.<br>
 * This class is based off the {@link AbstractAtlasSubCommand} by cstaylor.
 *
 * @author lcram
 * @author cstaylor
 */
public abstract class AtlasLoaderCommand extends AbstractAtlasShellToolsCommand
{
    private static final String INPUT_HINT = "input-atlases";

    private static final String COMBINE_OPTION_LONG = "combine";
    private static final String COMBINE_OPTION_DESCRIPTION = "Combine all input atlases into a MultiAtlas before processing.";

    private static final String STRICT_OPTION_LONG = "strict";
    private static final String STRICT_OPTION_DESCRIPTION = "Fail fast if any input atlases are missing.";

    private static final String OUTPUT_DIRECTORY_OPTION_LONG = "output";
    private static final Character OUTPUT_DIRECTORY_OPTION_SHORT = 'o';
    private static final String OUTPUT_DIRECTORY_OPTION_DESCRIPTION = "Specify an alternate output directory for any output files. If the directory "
            + "does not exist, it will be created.";
    private static final String OUTPUT_DIRECTORY_OPTION_HINT = "dir";

    private static final String PARALLEL_OPTION_LONG = "parallel";
    private static final Character PARALLEL_OPTION_SHORT = 'p';
    private static final String PARALLEL_OPTION_DESCRIPTION = "Process the atlases in parallel.";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    private List<File> atlasResourceList;
    private List<Atlas> atlasList;

    public AtlasLoaderCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
        this.atlasResourceList = null;
        this.atlasList = null;
    }

    @Override
    public int execute()
    {
        // call the user start implementation
        final int code = start();
        if (code != 0)
        {
            return code;
        }

        Stream<Atlas> atlasStream = getInputAtlases().stream();
        if (this.optionAndArgumentDelegate.hasOption(PARALLEL_OPTION_LONG))
        {
            atlasStream = atlasStream.parallel();
        }

        if (this.optionAndArgumentDelegate.hasOption(COMBINE_OPTION_LONG))
        {
            processAtlas(new MultiAtlas(atlasStream.collect(Collectors.toList())));
        }
        else
        {
            atlasStream.forEach(this::processAtlas);
        }

        // return the user's finish implementation
        return finish();
    }

    public String getFileName(final File file)
    {
        return file.getName();
    }

    public String getFileNameNoSuffix(final File file)
    {
        final String name = getFileName(file);
        final String[] split = name.split("\\.");
        return split[0];
    }

    public List<String> getFileNames(final List<File> files)
    {
        return files.stream().map(this::getFileName).collect(Collectors.toList());
    }

    public List<String> getFileNamesWithoutSuffixes(final List<File> files)
    {
        return files.stream().map(this::getFileNameNoSuffix).collect(Collectors.toList());
    }

    public Optional<Path> getOutputPath()
    {
        final Path outputParentPath = Paths.get(this.optionAndArgumentDelegate
                .getOptionArgument(OUTPUT_DIRECTORY_OPTION_LONG).orElse(""));

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
        addManualPageSection("ATLAS LOADER", AtlasLoaderCommand.class
                .getResourceAsStream("AtlasLoaderCommandLoaderSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        final Integer[] contexts = this.optionAndArgumentDelegate.getFilteredRegisteredContexts()
                .toArray(new Integer[0]);
        registerOption(STRICT_OPTION_LONG, STRICT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                contexts);
        registerOptionWithRequiredArgument(OUTPUT_DIRECTORY_OPTION_LONG,
                OUTPUT_DIRECTORY_OPTION_SHORT, OUTPUT_DIRECTORY_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, OUTPUT_DIRECTORY_OPTION_HINT, contexts);
        registerOption(PARALLEL_OPTION_LONG, PARALLEL_OPTION_SHORT, PARALLEL_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, contexts);
        registerOption(COMBINE_OPTION_LONG, COMBINE_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                contexts);
        registerArgument(INPUT_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED,
                contexts);
        super.registerOptionsAndArguments();
    }

    /**
     * After all atlas files have been handled, the subclass can override this method for final
     * notification and processing. The return value is sent back to the caller through System.exit
     *
     * @return a status value returned through System.exit
     */
    protected int finish()
    {
        return 0; // NOSONAR
    }

    /**
     * Subclasses can implement this method for processing each atlas object as it is loaded.
     *
     * @param atlas
     *            the atlas to process
     */
    protected abstract void processAtlas(Atlas atlas);

    /**
     * Subclasses can override this method if they want to do something once before processing the
     * atlases. The start method can return a status to indicate if the start-up operations were
     * successful. On return 0, the command will continue execution. On any non-zero exit code, the
     * command will terminate early and return the code through System.exit.
     *
     * @return a status value returned through System.exit
     */
    protected int start()
    {
        return 0;
    }

    /**
     * Get a list of atlas objects, one for each atlas loaded from the input-atlases parameter. This
     * method can be used when overriding the execute method, in place of the standard start(),
     * handle(), finish() semantics.
     *
     * @return the list of atlases
     */
    private List<Atlas> getInputAtlases()
    {
        if (this.atlasList == null)
        {
            this.atlasList = new ArrayList<>();
        }
        else
        {
            return this.atlasList;
        }

        // populate the list of atlas resources
        getInputAtlasResources();

        final AtlasResourceLoader loader = new AtlasResourceLoader();
        this.atlasResourceList.stream().forEach(atlasFile ->
        {
            final Atlas atlas = loader.load(atlasFile);
            if (atlas != null)
            {
                this.atlasList.add(atlas);
            }
        });

        return this.atlasList;
    }

    /**
     * Get a list of {@link File} resources, one for each atlas loaded from the input-atlases
     * parameter. This method can be used when overriding the execute method, in place of the
     * standard start(), handle(), finish() semantics.
     *
     * @return the list of atlas resources
     */
    private List<File> getInputAtlasResources()
    {
        final List<String> inputAtlasPaths = this.optionAndArgumentDelegate
                .getVariadicArgument(INPUT_HINT);

        if (this.atlasResourceList == null)
        {
            this.atlasResourceList = new ArrayList<>();
        }
        else
        {
            return this.atlasResourceList;
        }

        inputAtlasPaths.stream().forEach(path ->
        {
            final File file = new File(path);
            if (!file.exists())
            {
                this.outputDelegate.printlnWarnMessage("file not found: " + path);
            }
            else
            {
                if (this.optionAndArgumentDelegate.hasVerboseOption())
                {
                    this.outputDelegate.printlnCommandMessage("loading " + path);
                }
                this.atlasResourceList.add(file);
            }
        });

        if (this.optionAndArgumentDelegate.hasOption(STRICT_OPTION_LONG)
                && this.atlasResourceList.size() != inputAtlasPaths.size())
        {
            this.outputDelegate.printlnErrorMessage("strict load is some missing atlas(es)");
            this.atlasResourceList = new ArrayList<>();
        }

        return this.atlasResourceList;
    }
}

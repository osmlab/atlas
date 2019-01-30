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
import org.openstreetmap.atlas.utilities.tuples.Tuple;

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
    private static final String COMBINED_ATLAS_NAME = "combined.atlas";

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

    private List<Tuple<File, Atlas>> atlases;
    private Path outputPath;

    public static String removeSuffixFromFileName(final String fileName)
    {
        final String[] split = fileName.split("\\.");
        return split[0];
    }

    public AtlasLoaderCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
        this.atlases = null;
        this.outputPath = null;
    }

    @Override
    public int execute()
    {
        final Optional<Path> outputPathOptional = parseOutputPath();
        if (!outputPathOptional.isPresent())
        {
            this.outputDelegate.printlnErrorMessage("invalid output path");
            return 1;
        }
        else
        {
            this.outputPath = outputPathOptional.get();
        }

        // call the user start implementation
        final int code = start();
        if (code != 0)
        {
            return code;
        }

        Stream<Tuple<File, Atlas>> atlasTupleStream = getInputAtlases().stream();
        if (this.optionAndArgumentDelegate.hasOption(PARALLEL_OPTION_LONG))
        {
            atlasTupleStream = atlasTupleStream.parallel();
        }

        if (this.optionAndArgumentDelegate.hasOption(COMBINE_OPTION_LONG))
        {
            processAtlas(
                    new MultiAtlas(
                            atlasTupleStream.map(Tuple::getSecond).collect(Collectors.toList())),
                    COMBINED_ATLAS_NAME);
        }
        else
        {
            atlasTupleStream.forEach(atlasTuple -> processAtlas(atlasTuple.getSecond(),
                    atlasTuple.getFirst().getName()));
        }

        // return the user's finish implementation
        return finish();
    }

    public Path getOutputPath()
    {
        return this.outputPath;
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
     * @param atlasFileName
     *            name of the atlas file resource
     */
    protected abstract void processAtlas(Atlas atlas, String atlasFileName);

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
        return 0; // NOSONAR
    }

    /**
     * Get a list of input atlas resources with their associated atlases, one for each atlas loaded
     * from the input-atlases parameter. This method can be used when overriding the execute method,
     * in place of the standard start(), handle(), finish() semantics.
     *
     * @return the list of atlases
     */
    private List<Tuple<File, Atlas>> getInputAtlases()
    {
        if (this.atlases == null)
        {
            this.atlases = new ArrayList<>();
        }
        else
        {
            return this.atlases;
        }

        final List<String> inputAtlasPaths = this.optionAndArgumentDelegate
                .getVariadicArgument(INPUT_HINT);

        final AtlasResourceLoader loader = new AtlasResourceLoader();
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
                final Atlas atlas = loader.load(file);
                if (atlas != null)
                {
                    this.atlases.add(new Tuple<>(file, atlas));
                }
                else
                {
                    this.outputDelegate.printlnWarnMessage("could not load: " + file);
                }
            }
        });

        if (this.optionAndArgumentDelegate.hasOption(STRICT_OPTION_LONG)
                && this.atlases.size() != inputAtlasPaths.size())
        {
            this.outputDelegate.printlnErrorMessage("strict load is some missing atlas(es)");
            this.atlases = new ArrayList<>();
        }

        return this.atlases;
    }

    private Optional<Path> parseOutputPath()
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
}

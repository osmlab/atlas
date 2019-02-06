package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.command.AbstractAtlasSubCommand;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * A helper super class for any command that wants to load atlas files from disk. Provides a builtin
 * variadic input argument, as well as automatic conversion from paths to resources with various
 * options for increased flexibility. Subclasses can override the start(), processAtlas(), and
 * finish() methods, which provide a way to operate on the input atlases without having to deal with
 * resource loading and iterating.<br>
 * This class is based off the {@link AbstractAtlasSubCommand} by cstaylor.
 *
 * @author lcram
 * @author cstaylor
 */
public abstract class AtlasLoaderCommand extends MultipleOutputCommand
{
    private static final String COMBINED_ATLAS_NAME = "combined.atlas";

    private static final String INPUT_HINT = "input-atlases";

    private static final String COMBINE_OPTION_LONG = "combine";
    private static final String COMBINE_OPTION_DESCRIPTION = "Combine all input atlases into a MultiAtlas before processing.";

    private static final String STRICT_OPTION_LONG = "strict";
    private static final String STRICT_OPTION_DESCRIPTION = "Fail fast if any input atlases are missing.";

    private static final String PARALLEL_OPTION_LONG = "parallel";
    private static final Character PARALLEL_OPTION_SHORT = 'p';
    private static final String PARALLEL_OPTION_DESCRIPTION = "Process the atlases in parallel.";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    private List<Tuple<File, Atlas>> atlases;

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
    }

    @Override
    public int execute()
    {
        // set up the output path from the parent class
        int code = super.execute();
        if (code != 0)
        {
            return code;
        }

        // call the user start implementation
        code = start();
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
                    COMBINED_ATLAS_NAME, new File(COMBINED_ATLAS_NAME));
        }
        else
        {
            atlasTupleStream.forEach(atlasTuple -> processAtlas(atlasTuple.getSecond(),
                    atlasTuple.getFirst().getName(), atlasTuple.getFirst()));
        }

        // return the exit code from the user's finish implementation
        return finish();
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("ATLAS LOADER",
                AtlasLoaderCommand.class.getResourceAsStream("AtlasLoaderCommandSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        final Integer[] contexts = this.optionAndArgumentDelegate.getFilteredRegisteredContexts()
                .toArray(new Integer[0]);
        registerOption(STRICT_OPTION_LONG, STRICT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                contexts);
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
     * @param atlasResource
     *            the {@link File} resource from which the atlas was loaded
     */
    protected abstract void processAtlas(Atlas atlas, String atlasFileName, File atlasResource);

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
            else if (file.isDirectory())
            {
                this.outputDelegate.printlnWarnMessage("skipping directory: " + path);
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
            this.outputDelegate.printlnErrorMessage("strict load is missing some atlas(es)");
            this.atlases = new ArrayList<>();
        }

        return this.atlases;
    }
}

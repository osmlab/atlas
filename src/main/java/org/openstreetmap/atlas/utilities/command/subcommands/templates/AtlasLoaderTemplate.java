package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AtlasShellToolsCommandTemplate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.function.TernaryConsumer;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * An {@link AtlasShellToolsCommandTemplate} for commands that want to read and process some input
 * {@link Atlas}es.
 *
 * @author lcram
 */
public class AtlasLoaderTemplate implements AtlasShellToolsCommandTemplate
{
    private static final String COMBINED_ATLAS_NAME = "combined.atlas";

    private static final String INPUT_HINT = "input-atlases";
    private static final String COMBINE_OPTION_LONG = "combine";
    private static final String STRICT_OPTION_LONG = "strict";
    private static final String PARALLEL_OPTION_LONG = "parallel";
    private static final Character PARALLEL_OPTION_SHORT = 'p';

    private final Integer[] contexts;

    /**
     * Execute a command using the {@link AtlasLoaderTemplate} in a structured way. This is entirely
     * optional, but highly recommended as it handles boilerplate functionality for you
     * automatically.
     * 
     * @param parentCommand
     *            the parent command that controls this template
     * @param startUpFunction
     *            Provide this function if want to do something once before processing the atlases.
     *            The start function can return a status to indicate if the start-up operations were
     *            successful. On return 0, the command will continue execution. On any non-zero exit
     *            code, the parent function will return this function's exit value.
     * @param processAtlasFunction
     *            This function processes each atlas object as it is loaded. It is not not optional.
     *            The processAtlasFunction receives as arguments an Atlas object, a String name of
     *            the Atlas file resource, and the File resource object from which the Atlas was
     *            loaded.
     * @param finishUpFunction
     *            Provide this method to run after all atlas files have been handled for final
     *            notification and processing. Of this function will be sent back to the caller of
     *            the parent function.
     * @return An exit value for the command. Callers can simply return this from their execute
     *         methods.
     */
    public static int execute(final AbstractAtlasShellToolsCommand parentCommand,
            final IntSupplier startUpFunction,
            final TernaryConsumer<Atlas, String, File> processAtlasFunction,
            final IntSupplier finishUpFunction)
    {
        Objects.requireNonNull(processAtlasFunction);

        /*
         * Run the user's optionally supplied start-up function.
         */
        if (startUpFunction != null)
        {
            final int returnCode = startUpFunction.getAsInt();
            if (returnCode != 0)
            {
                return returnCode;
            }
        }

        /*
         * Get the input atlases and run the process code on each. We optionally collapse into a
         * MultiAtlas or stream parallel, depending on user options.
         */
        final List<Tuple<File, Atlas>> atlasTuples = getInputAtlases(parentCommand);
        if (atlasTuples.isEmpty())
        {
            parentCommand.getCommandOutputDelegate()
                    .printlnErrorMessage("no atlas files were loaded");
            return 1;
        }

        Stream<Tuple<File, Atlas>> atlasTupleStream = atlasTuples.stream();
        if (parentCommand.getOptionAndArgumentDelegate().hasOption(PARALLEL_OPTION_LONG))
        {
            atlasTupleStream = atlasTupleStream.parallel();
        }

        if (parentCommand.getOptionAndArgumentDelegate().hasOption(COMBINE_OPTION_LONG))
        {
            if (parentCommand.getOptionAndArgumentDelegate().hasVerboseOption())
            {
                parentCommand.getCommandOutputDelegate()
                        .printlnCommandMessage("processing all atlases as one multiatlas...");
            }
            processAtlasFunction.accept(
                    new MultiAtlas(
                            atlasTupleStream.map(Tuple::getSecond).collect(Collectors.toList())),
                    COMBINED_ATLAS_NAME,
                    new File(COMBINED_ATLAS_NAME, parentCommand.getFileSystem()));
        }
        else
        {
            final int size = atlasTuples.size();
            final int[] count = { 1 };
            atlasTupleStream.forEach(atlasTuple ->
            {
                if (parentCommand.getOptionAndArgumentDelegate().hasVerboseOption())
                {
                    parentCommand.getCommandOutputDelegate()
                            .printlnCommandMessage("processing atlas "
                                    + atlasTuple.getFirst().getAbsolutePathString() + " ("
                                    + count[0] + "/" + size + ")");
                }
                processAtlasFunction.accept(atlasTuple.getSecond(), atlasTuple.getFirst().getName(),
                        atlasTuple.getFirst());
                count[0]++;
            });
        }

        /*
         * Run the user's optionally supplied finish-up function.
         */
        if (finishUpFunction != null)
        {
            return finishUpFunction.getAsInt();
        }

        return 0;
    }

    /**
     * Get a list of input atlas resources with their associated atlases, one for each atlas loaded
     * from the input-atlases parameter.
     *
     * @return the list of atlases
     */
    private static List<Tuple<File, Atlas>> getInputAtlases(
            final AbstractAtlasShellToolsCommand parentCommand)
    {
        final List<Tuple<File, Atlas>> atlases = new ArrayList<>();

        final List<String> inputAtlasPaths = parentCommand.getOptionAndArgumentDelegate()
                .getVariadicArgument(INPUT_HINT);

        final AtlasResourceLoader loader = new AtlasResourceLoader();
        inputAtlasPaths.forEach(path ->
        {
            final File file = new File(path, parentCommand.getFileSystem(), false);
            if (!file.exists())
            {
                parentCommand.getCommandOutputDelegate()
                        .printlnWarnMessage("file not found: " + path);
            }
            else if (file.isDirectory())
            {
                parentCommand.getCommandOutputDelegate()
                        .printlnWarnMessage("skipping directory: " + path);
            }
            else
            {
                if (parentCommand.getOptionAndArgumentDelegate().hasVerboseOption())
                {
                    parentCommand.getCommandOutputDelegate()
                            .printlnCommandMessage("loading " + path);
                }
                final Optional<Atlas> atlas = loader.safeLoad(file);
                if (atlas.isPresent())
                {
                    atlases.add(new Tuple<>(file, atlas.get()));
                }
                else
                {
                    parentCommand.getCommandOutputDelegate()
                            .printlnWarnMessage("could not load: " + file);
                }
            }
        });

        if (parentCommand.getOptionAndArgumentDelegate().hasOption(STRICT_OPTION_LONG)
                && atlases.size() != inputAtlasPaths.size())
        {
            parentCommand.getCommandOutputDelegate()
                    .printlnErrorMessage("strict load is missing some atlas(es)");
            atlases.clear();
        }

        return atlases;
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
    public AtlasLoaderTemplate(final Integer... contexts)
    {
        this.contexts = contexts;
    }

    @Override
    public void registerManualPageSections(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.addManualPageSection("ATLAS LOADER",
                AtlasLoaderTemplate.class.getResourceAsStream("AtlasLoaderTemplateSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.registerOption(STRICT_OPTION_LONG,
                "Fail fast if any input atlases are missing.", OptionOptionality.OPTIONAL,
                this.contexts);
        parentCommand.registerOption(PARALLEL_OPTION_LONG, PARALLEL_OPTION_SHORT,
                "Process the atlases in parallel.", OptionOptionality.OPTIONAL, this.contexts);
        parentCommand.registerOption(COMBINE_OPTION_LONG,
                "Combine all input atlases into a MultiAtlas before processing.",
                OptionOptionality.OPTIONAL, this.contexts);
        parentCommand.registerArgument(INPUT_HINT, ArgumentArity.VARIADIC,
                ArgumentOptionality.REQUIRED, this.contexts);
    }
}

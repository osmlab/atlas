package org.openstreetmap.atlas.geography.atlas.command;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command.Flag;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

/**
 * Helper class that makes it easier to implement ReaderCommands that need loaded atlases
 *
 * @author cstaylor
 */
public abstract class AbstractAtlasSubCommand implements FlexibleSubCommand
{
    private static final Switch<File> INPUT_FOLDER_PARAMETER = new Switch<>("input",
            "Input atlas file or folder containing atlas files to load", File::new,
            Optionality.REQUIRED);

    private static final Flag COMBINE_PARAMETER = new Flag("combine",
            "Will combine all atlases found into a MultiAtlas before reading the metadata");

    private static final Flag PARALLEL_FLAG = new Flag("parallel",
            "Will process multiple atlases in parallel");

    private final String name;

    private final String description;

    protected AbstractAtlasSubCommand(final String name, final String description)
    {
        this.name = name;
        this.description = description;
    }

    @Override
    public int execute(final CommandMap command)
    {
        start(command);
        final File path = (File) command.get(INPUT_FOLDER_PARAMETER);

        Stream<Atlas> atlases = path.listFilesRecursively().stream()
                .filter(AtlasResourceLoader.IS_ATLAS)
                .map(atlas -> new AtlasResourceLoader().load(atlas));

        if ((Boolean) command.get(PARALLEL_FLAG))
        {
            atlases = atlases.parallel();
        }

        if ((Boolean) command.get(COMBINE_PARAMETER))
        {
            handle(new MultiAtlas(atlases.collect(Collectors.toList())), command);
        }
        else
        {
            atlases.forEach(atlas ->
            {
                handle(atlas, command);
            });
        }
        return finish(command);
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public final String getName()
    {
        return this.name;
    }

    @Override
    public SwitchList switches()
    {
        return new SwitchList().with(INPUT_FOLDER_PARAMETER, COMBINE_PARAMETER, PARALLEL_FLAG);
    }

    /**
     * After all atlas files have been handled, the subclass can override this method for a final
     * notification and processing. The return value is sent back to the caller through System.exit
     *
     * @param command
     *            arguments to this subcommand that may affect processing
     * @return a status value retured through System.exit
     */
    protected int finish(final CommandMap command)
    {
        return 0;
    }

    /**
     * Subclasses will implement this method for processing each atlas object as it is loaded.
     *
     * @param atlas
     *            the atlas to process
     * @param command
     *            arguments to this subcommand that may affect processing
     */
    protected abstract void handle(Atlas atlas, CommandMap command);

    /**
     * Subclasses can override this method if they want to do something once before processing all
     * of the atlases
     *
     * @param command
     *            arguments to this subcommand that may affect processing
     */
    protected void start(final CommandMap command)
    {

    }

}

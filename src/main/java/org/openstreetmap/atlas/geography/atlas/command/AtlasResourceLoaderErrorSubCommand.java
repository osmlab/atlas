package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.io.StreamCorruptedException;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.exception.ExceptionSearch;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

/**
 * Quick way of testing various problems when loading atlas files (missing files, corrupt files,
 * etc...)
 *
 * @author cstaylor
 */
public class AtlasResourceLoaderErrorSubCommand implements FlexibleSubCommand
{
    private static final Switch<File> INPUT_PARAMETER = new Switch<>("input", "Path of Atlas file",
            File::new, Command.Optionality.OPTIONAL);

    @Override
    public int execute(final CommandMap map)
    {
        final File input = (File) map.get(INPUT_PARAMETER);
        try
        {
            new AtlasResourceLoader().load(input);
        }
        catch (final CoreException oops)
        {
            oops.printStackTrace();
            ExceptionSearch.find(StreamCorruptedException.class).within(oops)
                    .ifPresent(StreamCorruptedException::printStackTrace);
        }
        return 0;
    }

    @Override
    public String getDescription()
    {
        return "Testing the abstract resource loader";
    }

    @Override
    public String getName()
    {
        return "resource-load-testing";
    }

    @Override
    public SwitchList switches()
    {
        return new SwitchList().with(INPUT_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf("-input=/path/to/resources/for/loading/atlas/files");
    }

}

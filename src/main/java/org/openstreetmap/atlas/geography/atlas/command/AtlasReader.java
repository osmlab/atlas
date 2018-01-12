package org.openstreetmap.atlas.geography.atlas.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.openstreetmap.atlas.utilities.runtime.FlexibleCommand;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

/**
 * Shell for running atlas commands. Run this command with no arguments to learn more about it.
 *
 * @author cstaylor
 */
public class AtlasReader extends FlexibleCommand
{
    public static void main(final String... args)
    {
        final AtlasReader reader = new AtlasReader(args);
        try
        {
            reader.runWithoutQuitting(args);
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
            reader.printUsageAndExit(1);
        }
    }

    public AtlasReader(final String... args)
    {
        super(args);
    }

    @Override
    protected Stream<Class<? extends FlexibleSubCommand>> getSupportedCommands()
    {
        final List<Class<? extends FlexibleSubCommand>> returnValue = new ArrayList<>();
        new FastClasspathScanner(AtlasReader.class.getPackage().getName())
                .matchClassesImplementing(FlexibleSubCommand.class, returnValue::add).scan();
        return returnValue.stream();
    }
}

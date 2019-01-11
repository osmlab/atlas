package org.openstreetmap.atlas.geography.atlas.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.openstreetmap.atlas.utilities.runtime.FlexibleCommand;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

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
        catch (final Exception e)
        {
            e.printStackTrace();
            reader.printUsageAndExit(1);
        }
    }

    public AtlasReader(final String... args)
    {
        super(args);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Stream<Class<? extends FlexibleSubCommand>> getSupportedCommands()
    {
        final List<Class<? extends FlexibleSubCommand>> returnValue = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo()
                .whitelistPackages(AtlasReader.class.getPackage().getName()).scan())
        {
            final ClassInfoList classInfoList = scanResult
                    .getClassesImplementing(FlexibleSubCommand.class.getName());
            classInfoList.loadClasses()
                    .forEach(klass -> returnValue.add((Class<? extends FlexibleSubCommand>) klass));
        }
        return returnValue.stream();
    }
}

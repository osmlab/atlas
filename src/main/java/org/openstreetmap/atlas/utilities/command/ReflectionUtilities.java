package org.openstreetmap.atlas.utilities.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AtlasShellToolsMarkerInterface;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

/**
 * @author lcram
 */
public final class ReflectionUtilities
{
    @SuppressWarnings("unchecked")
    public static Set<AbstractAtlasShellToolsCommand> getSubcommandInstances()
    {
        final List<Class<? extends AtlasShellToolsMarkerInterface>> subcommandClasses = new ArrayList<>();
        final Set<AbstractAtlasShellToolsCommand> instantiatedCommands = new HashSet<>();
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan())
        {
            final ClassInfoList classInfoList = scanResult
                    .getClassesImplementing(AtlasShellToolsMarkerInterface.class.getName());
            classInfoList.loadClasses().forEach(klass -> subcommandClasses
                    .add((Class<? extends AtlasShellToolsMarkerInterface>) klass));
        }
        subcommandClasses.stream().forEach(klass -> instantiateSubcommand(klass.getName())
                .ifPresent(instantiatedCommands::add));
        return instantiatedCommands;
    }

    private static Optional<AbstractAtlasShellToolsCommand> instantiateSubcommand(
            final String classname)
    {
        final Class<?> subcommandClass;
        try
        {
            subcommandClass = Class.forName(classname);
        }
        catch (final ClassNotFoundException exception)
        {
            throw new CoreException("Class {} was not found", classname, exception);
        }

        if (Modifier.isAbstract(subcommandClass.getModifiers()))
        {
            return Optional.empty();
        }

        final Constructor<?> constructor;
        try
        {
            constructor = subcommandClass.getConstructor();
        }
        catch (final NoSuchMethodException exception)
        {
            throw new CoreException("Class {} does not have a matching constructor", classname,
                    exception);
        }
        catch (final SecurityException exception)
        {
            throw new CoreException("Error instantiating class {}", classname, exception);
        }

        final AbstractAtlasShellToolsCommand subcommand;
        try
        {
            subcommand = (AbstractAtlasShellToolsCommand) constructor.newInstance(new Object[] {}); // NOSONAR
        }
        catch (final ClassCastException exception)
        {
            throw new CoreException("Class {} not a subtype of {}", classname,
                    AbstractAtlasShellToolsCommand.class.getName(), exception);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Error instantiating class {}", classname, exception);
        }

        return Optional.of(subcommand);
    }

    private ReflectionUtilities()
    {

    }
}

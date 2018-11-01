package org.openstreetmap.atlas.utilities.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

/**
 * @author lcram
 */
public final class ReflectionUtilities
{
    public static Set<AbstractOSMSubcommand> getSubcommandInstances()
    {
        final Set<Class<? extends OSMSubcommand>> subcommandClasses = new HashSet<>();
        final Set<AbstractOSMSubcommand> instantiatedCommands = new HashSet<>();
        new FastClasspathScanner()
                .matchClassesImplementing(OSMSubcommand.class, subcommandClasses::add).scan();
        subcommandClasses.stream().forEach(klass ->
        {
            // final Optional<AbstractOSMSubcommand> commandOption = instantiateSubcommand(
            // klass.getName());
            // if (commandOption.isPresent())
            // {
            // instantiatedCommands.add(commandOption.get());
            // }
        });
        return instantiatedCommands;
    }

    private static Optional<AbstractOSMSubcommand> instantiateSubcommand(final String classname)
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

        final AbstractOSMSubcommand subcommand;
        try
        {
            subcommand = (AbstractOSMSubcommand) constructor.newInstance(new Object[] {});
        }
        catch (final ClassCastException exception)
        {
            throw new CoreException("Class {} not a subtype of {}", classname,
                    AbstractOSMSubcommand.class.getName(), exception);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Error instantiating class {}", classname, exception);
        }

        return Optional.of(subcommand);
    }
}

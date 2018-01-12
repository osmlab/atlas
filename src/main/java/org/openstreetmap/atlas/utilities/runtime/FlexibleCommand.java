package org.openstreetmap.atlas.utilities.runtime;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

/**
 * Shell for running subcommands. Reflections is used to find commands and their switchlists are
 * automatically added to the command at runtime. This implementation will search the entire
 * classloader for all FlexibleSubCommand implementations. If you want to restrict the set of
 * commands to support, override the getSupportedCommands method
 *
 * @author cstaylor
 */
public class FlexibleCommand extends Command
{
    private FlexibleSubCommand subcommand;

    private Map<String, ? extends FlexibleSubCommand> commandMap;

    public static void main(final String... args)
    {
        if (args.length == 0)
        {
            new FlexibleCommand("FAILED").printUsageAndExit();
        }
        new FlexibleCommand(args[0]).run(args);
    }

    protected static boolean noAbstract(final Class<? extends FlexibleSubCommand> klass)
    {
        return !Modifier.isAbstract(klass.getModifiers());
    }

    private static FlexibleSubCommand create(final Class<? extends FlexibleSubCommand> klass)
    {
        try
        {
            return klass.newInstance();
        }
        catch (final Exception oops)
        {
            throw new CoreException("Error when creating new instance of {}",
                    klass.getCanonicalName(), oops);
        }
    }

    public FlexibleCommand(final String... args)
    {
        initializeCommands();
        if (args.length == 0)
        {
            printUsageAndExit();
        }
    }

    @Override
    public void run(final String... arguments)
    {
        final String[] commandArgs = prepare(arguments);
        super.run(commandArgs);
    }

    @Override
    public void runWithoutQuitting(final String... arguments)
    {
        final String[] commandArgs = prepare(arguments);
        super.runWithoutQuitting(commandArgs);
    }

    /**
     * Default behavior finds all FlexibleSubCommands in the classpath. Override to restrict the set
     * of classes returned
     *
     * @return a stream containing all of the subcommands we want to support
     */
    protected Stream<Class<? extends FlexibleSubCommand>> getSupportedCommands()
    {
        final List<Class<? extends FlexibleSubCommand>> returnValue = new ArrayList<>();
        new FastClasspathScanner()
                .matchClassesImplementing(FlexibleSubCommand.class, returnValue::add).scan();
        return returnValue.stream();
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        return this.subcommand.execute(command);
    }

    protected void printUsageAndExit()
    {
        printUsageAndExit(-1);
    }

    protected void printUsageAndExit(final int errorCode)
    {
        this.commandMap.entrySet().stream().forEach(item ->
        {
            System.err.printf("%s - %s\n", item.getKey(), item.getValue().getDescription());
            item.getValue().usage(System.err);
            System.err.printf("\n");
        });
        System.exit(errorCode);
    }

    @Override
    protected SwitchList switches()
    {
        return this.subcommand.switches();
    }

    private void initializeCommands()
    {
        this.commandMap = getSupportedCommands().filter(FlexibleCommand::noAbstract)
                .map(FlexibleCommand::create)
                .collect(Collectors.toMap(FlexibleSubCommand::getName, x -> x));
    }

    private String[] prepare(final String... arguments)
    {
        this.subcommand = this.commandMap.get(arguments[0]);
        if (this.subcommand == null)
        {
            printUsageAndExit();
        }
        /**
         * With FlexibleCommands, the first argument is the name of the subcommand. We're peeling
         * that argument off the args array and passing that value to the FlexibleCommand
         * constructor. If no subcommand matches that name, we immediately terminate with a usage
         * error, otherwise we continue to run using the remaining arguments copied heres
         */
        final String[] commandArgs = new String[arguments.length - 1];
        System.arraycopy(arguments, 1, commandArgs, 0, commandArgs.length);
        return commandArgs;
    }
}

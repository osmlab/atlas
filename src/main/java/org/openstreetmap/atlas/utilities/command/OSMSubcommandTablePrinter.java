package org.openstreetmap.atlas.utilities.command;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author lcram
 */
public class OSMSubcommandTablePrinter
{
    private static final String VERBOSE = "--verbose";

    // Use ASCII record separator as delimiter
    private static final String DELIMITER = Character.toString((char) 0x1E);

    private final boolean useVerbose;

    public static void main(final String[] args)
    {
        String arg = null;
        if (args.length > 0)
        {
            arg = args[0];
        }
        if (Objects.equal(VERBOSE, arg))
        {
            new OSMSubcommandTablePrinter(true).printLookupTable();
        }
        else
        {
            new OSMSubcommandTablePrinter(false).printLookupTable();
        }

    }

    public OSMSubcommandTablePrinter(final boolean useVerbose)
    {
        this.useVerbose = useVerbose;
    }

    private void diagnosticIfVerbose(final String message)
    {
        if (this.useVerbose)
        {
            System.err.println(message);
        }
    }

    private void printLookupTable()
    {
        final Set<AbstractAtlasShellToolsCommand> commands = ReflectionUtilities
                .getSubcommandInstances();
        final Set<String> namesWeHaveAlreadySeen = new HashSet<>();

        // print a line break
        diagnosticIfVerbose("");
        for (final AbstractAtlasShellToolsCommand command : commands)
        {
            diagnosticIfVerbose("Found command definition in " + command.getClass().getName());
            diagnosticIfVerbose("Validating command definition...");

            // validate the command name and description
            command.throwIfInvalidNameOrDescription();

            // Validate the command options/args/manpage - will throw if something is awry
            command.registerOptionsAndArguments();
            command.registerManualPageSections();

            diagnosticIfVerbose("Generating index entry...");
            final StringBuilder builder = new StringBuilder();
            String name = command.getCommandName();
            String nameWithSuffix = name;
            int uniqueSuffix = 2;

            while (namesWeHaveAlreadySeen.contains(nameWithSuffix))
            {
                nameWithSuffix = name + uniqueSuffix;
                uniqueSuffix++;
            }
            name = nameWithSuffix;

            builder.append(name);
            namesWeHaveAlreadySeen.add(name);
            builder.append(DELIMITER);
            builder.append(command.getClass().getName());
            builder.append(DELIMITER);
            builder.append(command.getSimpleDescription());
            System.out.println(builder.toString());
            diagnosticIfVerbose("Command " + command.getCommandName() + " registered OK.");
            // print a line break
            diagnosticIfVerbose("");
        }
    }
}

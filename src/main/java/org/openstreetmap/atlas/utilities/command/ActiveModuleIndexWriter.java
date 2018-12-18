package org.openstreetmap.atlas.utilities.command;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;

import com.google.common.base.Objects;

/**
 * @author lcram
 */
public class ActiveModuleIndexWriter
{
    private static final String VERBOSE = "--verbose";

    // Use ASCII record separator as delimiter
    private static final String DELIMITER = Character.toString((char) 0x1E);

    private final boolean useVerbose;
    private final String outputPath;

    public static void main(final String[] args)
    {
        String outputPath = null;
        String verboseFlag = null;
        if (args.length < 1)
        {
            throw new CoreException("Missing required output path argument");
        }
        else if (args.length == 1)
        {
            outputPath = args[0];
        }
        else
        {
            outputPath = args[0];
            verboseFlag = args[1];
        }
        if (Objects.equal(VERBOSE, verboseFlag))
        {
            new ActiveModuleIndexWriter(outputPath, true).printLookupTable();
        }
        else
        {
            new ActiveModuleIndexWriter(outputPath, false).printLookupTable();
        }

    }

    public ActiveModuleIndexWriter(final String outputPath, final boolean useVerbose)
    {
        this.outputPath = outputPath;
        this.useVerbose = useVerbose;
    }

    private void diagnosticIfVerbose(final String message)
    {
        if (this.useVerbose)
        {
            System.out.println(message);
        }
    }

    private void printLookupTable()
    {
        final Set<AbstractAtlasShellToolsCommand> commands = ReflectionUtilities
                .getSubcommandInstances();
        final Set<String> namesWeHaveAlreadySeen = new HashSet<>();
        final PrintWriter printWriter;
        try
        {
            printWriter = new PrintWriter(new FileWriter(this.outputPath));
        }
        catch (final IOException exception)
        {
            throw new CoreException("Could not write index", exception);
        }

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
            printWriter.println(builder.toString());
            diagnosticIfVerbose("Command " + command.getCommandName() + " registered OK.");

            // print a line break
            diagnosticIfVerbose("");
        }
        printWriter.close();
    }
}

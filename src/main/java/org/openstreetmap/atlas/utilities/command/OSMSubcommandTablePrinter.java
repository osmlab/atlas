package org.openstreetmap.atlas.utilities.command;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lcram
 */
public class OSMSubcommandTablePrinter
{
    // Use ASCII record separator as delimiter
    private static final String DELIMITER = Character.toString((char) 0x1E);

    public static void main(final String[] args)
    {
        new OSMSubcommandTablePrinter().printLookupTable();
    }

    private void printLookupTable()
    {
        final Set<AbstractAtlasShellToolsCommand> commands = ReflectionUtilities.getSubcommandInstances();
        final Set<String> namesWeHaveAlreadySeen = new HashSet<>();
        for (final AbstractAtlasShellToolsCommand command : commands)
        {
            // validate the command name and description
            command.throwIfInvalidNameOrDescription();

            // Validate the command options/args/manpage - will throw if something is awry
            command.registerOptionsAndArguments();
            command.registerManualPageSections();

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
        }
    }
}

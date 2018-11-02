package org.openstreetmap.atlas.utilities.command;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lcram
 */
public class OSMSubcommandTablePrinter
{
    // Use ASCII record separator as delimiter
    private static final String DELIMITER = Character.toString((char) 0x1e);

    public static void main(final String[] args)
    {
        new OSMSubcommandTablePrinter().printLookupTable();
    }

    private void printLookupTable()
    {
        final Set<AbstractOSMSubcommand> commands = ReflectionUtilities.getSubcommandInstances();
        final Set<String> namesWeHaveAlreadySeen = new HashSet<>();
        for (final AbstractOSMSubcommand command : commands)
        {
            final StringBuilder builder = new StringBuilder();

            String name = command.getName();
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

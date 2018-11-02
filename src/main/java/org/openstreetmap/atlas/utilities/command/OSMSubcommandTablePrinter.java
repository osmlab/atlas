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
            int uniqueSuffix = 2;
            final StringBuilder builder = new StringBuilder();
            String name = command.getName();
            while (namesWeHaveAlreadySeen.contains(name))
            {
                name = name + uniqueSuffix;
                uniqueSuffix++;
            }
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

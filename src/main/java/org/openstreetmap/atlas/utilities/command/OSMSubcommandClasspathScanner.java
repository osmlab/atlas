package org.openstreetmap.atlas.utilities.command;

import java.util.Set;

/**
 * @author lcram
 */
public class OSMSubcommandClasspathScanner
{
    // Use ASCII record separator as delimiter
    private static final String DELIMITER = Character.toString((char) 0x1e);

    public static void main(final String[] args)
    {
        new OSMSubcommandClasspathScanner().scanAndPrintLookupTable();
    }

    private void scanAndPrintLookupTable()
    {
        final Set<AbstractOSMSubcommand> commands = ReflectionUtilities.getSubcommandInstances();
        for (final AbstractOSMSubcommand command : commands)
        {
            final StringBuilder builder = new StringBuilder();
            builder.append(command.getName());
            builder.append(DELIMITER);
            builder.append(command.getClass().getName());
            builder.append(DELIMITER);
            builder.append(command.getSimpleDescription());
            System.out.println(builder.toString());
        }
    }
}

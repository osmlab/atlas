package org.openstreetmap.atlas.utilities.command;

/**
 * @author lcram
 */
public abstract class AbstractOSMSubcommand implements OSMSubcommand
{
    public abstract int execute();

    public abstract String getName();

    public abstract String getSimpleDescription();

    public abstract void registerOptions();

    protected String getHelpMenu()
    {
        final String name = this.getName();
        final String simpleDescription = this.getSimpleDescription();
        return "NAME\n\t" + name + " -- " + simpleDescription + "\n";
    }

    protected void runSubcommandAndExit(final String[] args)
    {
        // fill out appropriate data structures so the execute() implementation can query
        registerOptions();

        // dummy HELP option implementation
        if (args.length > 0)
        {
            if ("--help".equals(args[0]) || "-h".equals(args[0]))
            {
                System.err.println(this.getHelpMenu());
                System.exit(1);
            }
        }

        // run the command
        System.exit(execute());
    }
}

package org.openstreetmap.atlas.utilities.command;

import java.util.Arrays;

/**
 * @author lcram
 */
public abstract class AbstractOSMSubcommand implements OSMSubcommand
{
    /*
     * Until Java supports the ability to do granular TTY checking thru an interface like isatty(2),
     * we must rely on a sentinel. An external wrapper (bash, perl, etc.) can do the necessary TTY
     * checking, and then pass this sentinel to tell the subcommand if it should use special
     * formatting. A ticket to support better TTY in Java checking has been open for years, with no
     * avail: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4099017
     */
    private static final String NO_COLOR_OPTION = "___AbstractOSMSubcommand__NO_COLOR_SPECIAL_ARG___";
    private static final String ANSI_BOLD = "\033[1m";
    private static final String ANSI_RESET = "\033[0m";

    private boolean useColor = true;

    public abstract int execute();

    public abstract String getCommandName();

    public abstract String getSimpleDescription();

    public abstract void registerOptionsAndArguments();

    protected String getHelpMenu()
    {
        final String name = this.getCommandName();
        final String simpleDescription = this.getSimpleDescription();
        final StringBuilder builder = new StringBuilder();
        builder.append("\n");
        if (this.useColor)
        {
            builder.append(ANSI_BOLD);
        }
        builder.append("NAME");
        if (this.useColor)
        {
            builder.append(ANSI_RESET);
        }
        builder.append("\n\t" + name + " -- " + simpleDescription + "\n");
        builder.append("\n");
        return builder.toString();
    }

    protected void runSubcommandAndExit(String... args)
    {
        // check the last arg to see if we should disable colors
        if (args.length > 0 && NO_COLOR_OPTION.equals(args[args.length - 1]))
        {
            this.useColor = false;
            args = Arrays.copyOf(args, args.length - 1);
        }

        // fill out appropriate data structures so the execute() implementation can query
        registerOptionsAndArguments();

        // dummy HELP option implementation
        if (args.length > 0)
        {
            if ("--help".equals(args[0]) || "-h".equals(args[0]))
            {
                System.out.println(this.getHelpMenu());
                System.exit(0);
            }
        }

        // run the command
        System.exit(execute());
    }
}

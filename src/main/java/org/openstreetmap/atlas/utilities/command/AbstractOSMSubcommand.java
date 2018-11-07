package org.openstreetmap.atlas.utilities.command;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.OptionParseException;
import org.openstreetmap.atlas.utilities.command.SimpleOptionAndArgumentParser.UnknownOptionException;

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

    private final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();

    private boolean useColor = true;

    public abstract int execute();

    public abstract String getCommandName();

    public abstract String getSimpleDescription();

    public abstract void registerOptionsAndArguments();

    protected List<String> getArgumentForHint(final String hint)
    {
        return this.parser.getArgumentForHint(hint);
    }

    protected Optional<String> getLongOptionArgument(final String longForm)
    {
        return this.parser.getLongOptionArgument(longForm);
    }

    protected boolean hasOption(final String longForm)
    {
        return this.parser.hasOption(longForm);
    }

    protected boolean hasShortOption(final Character shortForm)
    {
        return this.parser.hasShortOption(shortForm);
    }

    protected void registerArgument(final String argumentHint, final ArgumentArity parity)
    {
        this.parser.registerArgument(argumentHint, parity);
    }

    protected void registerOption(final String longForm, final Character shortForm,
            final String description)
    {
        if ("help".equals(longForm) || shortForm == 'h')
        {
            throw new CoreException("Cannot reregister builtin option (help,h)");
        }
        this.parser.registerOption(longForm, shortForm, description);
    }

    protected void registerOption(final String longForm, final String description)
    {
        if ("help".equals(longForm))
        {
            throw new CoreException("Cannot reregister builtin option (help,h)");
        }
        this.parser.registerOption(longForm, description);
    }

    protected void registerOptionWithOptionalArgument(final String longForm,
            final String description, final String argumentHint)
    {
        if ("help".equals(longForm))
        {
            throw new CoreException("Cannot reregister builtin option (help,h)");
        }
        this.parser.registerOptionWithOptionalArgument(longForm, description, argumentHint);
    }

    protected void registerOptionWithRequiredArgument(final String longForm,
            final String description, final String argumentHint)
    {
        if ("help".equals(longForm))
        {
            throw new CoreException("Cannot reregister builtin option (help,h)");
        }
        this.parser.registerOptionWithRequiredArgument(longForm, description, argumentHint);
    }

    protected void runSubcommandAndExit(String... args)
    {
        this.parser.registerOption("help", 'h', "Show this help menu.");

        // check the last arg to see if we should disable colors
        if (args.length > 0 && NO_COLOR_OPTION.equals(args[args.length - 1]))
        {
            this.useColor = false;
            args = Arrays.copyOf(args, args.length - 1);
        }

        // fill out appropriate data structures so the execute() implementation can query
        registerOptionsAndArguments();

        // Special case if user supplied '--help' or '-h'
        // We want to scan now, show the help menu, then abort
        if (this.parser.scanForHelpFlag(Arrays.asList(args)))
        {
            System.out.println(this.getHelpMenu());
            System.exit(0);
        }

        try
        {
            this.parser.parseOptionsAndArguments(Arrays.asList(args));
        }
        catch (final UnknownOptionException exception)
        {
            // TODO colorize
            System.err.println(
                    this.getCommandName() + ": error: unknown option " + exception.getMessage());
            System.exit(1);
        }
        catch (final OptionParseException exception)
        {
            // TODO colorize
            System.err.println(this.getCommandName() + ": error: " + exception.getMessage());
            System.exit(1);
        }

        // run the command
        System.exit(execute());
    }

    private String getHelpMenu()
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
}

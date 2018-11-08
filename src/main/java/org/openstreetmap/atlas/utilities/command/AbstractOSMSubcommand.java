package org.openstreetmap.atlas.utilities.command;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.ArgumentException;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.OptionParseException;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.UnknownOptionException;

/**
 * A partial implementation of an OSM subcommand. Contains significant functionality to aid in
 * command development, including some builtin options.
 *
 * @author lcram
 */
public abstract class AbstractOSMSubcommand implements OSMSubcommand
{
    /*
     * Until Java supports the ability to do granular TTY checking thru an interface like isatty(3),
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
    private String version = "default_version_value";

    /**
     * Execute the command logic. Subclasses of {@link AbstractOSMSubcommand} must implement this
     * method, but in general it should not be called directly. See
     * {@link AbstractOSMSubcommand#runSubcommandAndExit(String...)}.
     *
     * @return the return code of the command
     */
    public abstract int execute();

    /**
     * The simple name of the command. This should be easy to type for ease of command line use.
     *
     * @return the simple name of the command
     */
    public abstract String getCommandName();

    /**
     * A simple description of the command. It should be brief - see the NAME section of any man
     * page for an example.
     *
     * @return the description
     */
    public abstract String getSimpleDescription();

    /**
     * Register any necessary options and arguments for the command. Use the protected API exposed
     * by {@link AbstractOSMSubcommand}.
     */
    public abstract void registerOptionsAndArguments();

    /*
     * BEGIN ADAPTER OPTION/ARGUMENT PARSING INTERFACE
     */
    // While this may seem like duplication of the SimpleOptionAndArgumentParser interface,
    // it actually allows us to define an immutable interface for subcommand registration. By
    // setting up the interface this way, we are not wedded to the SimpleOptionAndArgumentParser for
    // future changes. Should we decide to change it, any subcommands implementing
    // AbstractOSMSubcommand will not have to change their option registration code.

    /**
     * Get the argument of a given long option, if present.
     *
     * @param longForm
     *            the long form of the option
     * @return an {@link Optional} wrapping the argument
     * @throws CoreException
     *             if longForm does not refer to a registered option
     */
    protected Optional<String> getLongOptionArgument(final String longForm)
    {
        return this.parser.getLongOptionArgument(longForm);
    }

    /**
     * Given a hint registered as a unary argument, return an optional wrapping the argument value
     * associated with that hint.
     *
     * @param hint
     *            the hint to check
     * @return an {@link Optional} wrapping the value
     * @throws CoreException
     *             if the argument hint was not registered or is not unary
     */
    protected Optional<String> getUnaryArgument(final String hint)
    {
        return this.parser.getUnaryArgument(hint);
    }

    /**
     * Given a hint registered as a variadic argument, return the argument values associated with
     * that hint.
     *
     * @param hint
     *            the hint to check
     * @return a list of the values
     * @throws CoreException
     *             if the argument hint was not registered or is not variadic
     */
    protected List<String> getVariadicArgument(final String hint)
    {
        return this.parser.getVariadicArgument(hint);
    }

    /**
     * Check if a given option was supplied. This will return true even if only the short form was
     * actually present on the command line.
     *
     * @param longForm
     *            the option
     * @return if the option was supplied
     * @throws CoreException
     *             if longForm does not refer to a registered option
     */
    protected boolean hasOption(final String longForm)
    {
        return this.parser.hasOption(longForm);
    }

    /**
     * Register an argument with a given arity. The argument hint is used as a key to retrieve the
     * argument value(s) later. Additionally, documentation can use the hint to specify what the
     * argument should be for. The arity is defined by {@link ArgumentArity}.
     *
     * @param argumentHint
     *            the hint for the argument
     * @param arity
     *            the argument arity
     * @param type
     *            the argument hint type, optional or required
     */
    protected void registerArgument(final String argumentHint, final ArgumentArity arity,
            final ArgumentOptionality type)
    {
        this.parser.registerArgument(argumentHint, arity, type);
    }

    /**
     * Register an option with a given long and short form. The option will be a flag option, ie. it
     * can take no arguments.
     *
     * @param longForm
     *            the long form of the option, eg. --option
     * @param shortForm
     *            the short form of the option, eg. -o
     * @param description
     *            a simple description
     */
    protected void registerOption(final String longForm, final Character shortForm,
            final String description)
    {
        this.parser.registerOption(longForm, shortForm, description);
    }

    /**
     * Register an option with a given long form. The option will be a flag option, ie. it can take
     * no arguments.
     *
     * @param longForm
     *            the long form of the option, eg. --option
     * @param description
     *            a simple description
     */
    protected void registerOption(final String longForm, final String description)
    {
        this.parser.registerOption(longForm, description);
    }

    /**
     * Register an option with a given long form that takes an optional argument. The provided
     * argument hint can be used for generated documentation, and should be a single word describing
     * the argument.
     *
     * @param longForm
     *            the long form of the option, eg. --option
     * @param description
     *            a simple description
     * @param argumentHint
     *            the hint for the argument
     */
    protected void registerOptionWithOptionalArgument(final String longForm,
            final String description, final String argumentHint)
    {
        this.parser.registerOptionWithOptionalArgument(longForm, description, argumentHint);
    }

    /**
     * Register an option with a given long form that takes a required argument. The provided
     * argument hint can be used for generated documentation, and should be a single word describing
     * the argument. The parser will throw an exception if a required argument option is not
     * supplied an argument at parse-time.
     *
     * @param longForm
     *            the long form of the option, eg. --option
     * @param description
     *            a simple description
     * @param argumentHint
     *            the hint for the argument
     */
    protected void registerOptionWithRequiredArgument(final String longForm,
            final String description, final String argumentHint)
    {
        this.parser.registerOptionWithRequiredArgument(longForm, description, argumentHint);
    }

    /*
     * END FACADE OPTION/ARGUMENT PARSING INTERFACE
     */

    /**
     * Run this subcommand using all the special setup and teardown semantics provided by
     * {@link AbstractOSMSubcommand}. It automatically registers some default standard arguments:
     * (help,h) and (verbose,v). An example of how this method should be called from main to make
     * the command functional with an external wrapper.
     *
     * <pre>
     * public static void main(final String[] args)
     * {
     *     new MySubclassSubcommand().runSubcommandAndExit(args);
     * }
     * </pre>
     *
     * @param args
     *            the command arguments
     */
    protected void runSubcommandAndExit(String... args)
    {
        this.parser.registerOption("help", 'h', "Show this help menu.");
        this.parser.registerOption("verbose", 'v', "Use verbose output.");

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

        // Special case if user supplied '--version' or '-V'
        // We want to scan now, show the version, then abort
        if (this.parser.scanForVersionFlag(Arrays.asList(args)))
        {
            System.out.println(this.getCommandName() + " " + this.version);
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
        catch (final ArgumentException exception)
        {
            // TODO colorize
            System.err.println(this.getCommandName() + ": error: " + exception.getMessage());
            System.exit(1);
        }

        // run the command
        System.exit(execute());
    }

    /**
     * Set the version of this command. Also automatically registers a version option.
     */
    protected void setVersion(final String version)
    {
        this.parser.registerOption("version", 'V',
                "Print the version of " + this.getCommandName() + " and exit.");
        this.version = version;
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

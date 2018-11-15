package org.openstreetmap.atlas.utilities.command;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.output.TTYAttribute;
import org.openstreetmap.atlas.utilities.command.output.TTYStringBuilder;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.ArgumentException;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.OptionParseException;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.UnknownOptionException;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;

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

    private static final String DEFAULT_HELP_LONG = "help";
    private static final Character DEFAULT_HELP_SHORT = 'h';
    private static final String DEFAULT_VERBOSE_LONG = "verbose";
    private static final Character DEFAULT_VERBOSE_SHORT = 'v';

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
     * A NOTE ON THE ADAPTER OPTION/ARGUMENT PARSING INTERFACE
     */
    // While this may seem like duplication of the SimpleOptionAndArgumentParser interface,
    // it actually allows us to define an immutable interface for subcommand registration. By
    // setting up the interface this way, we are not wedded to the SimpleOptionAndArgumentParser for
    // future changes. Should we decide to change it, any subcommands implementing
    // AbstractOSMSubcommand will not have to change their option registration code.

    /**
     * Get the argument of a given option, if present.
     *
     * @param longForm
     *            the long form of the option
     * @return an {@link Optional} wrapping the argument
     * @throws CoreException
     *             if longForm does not refer to a registered option
     */
    protected Optional<String> getOptionArgument(final String longForm)
    {
        return this.parser.getOptionArgument(longForm);
    }

    /**
     * Get the argument of a given option, if present. Also, convert it using the supplied
     * converter.
     *
     * @param <T>
     *            the type to convert to
     * @param longForm
     *            the long form of the option
     * @param converter
     *            the conversion function
     * @return an {@link Optional} wrapping the argument
     * @throws CoreException
     *             if longForm does not refer to a registered option
     */
    protected <T> Optional<T> getOptionArgument(final String longForm,
            final StringConverter<T> converter)
    {
        return this.parser.getOptionArgument(longForm, converter);
    }

    /**
     * Get a {@link TTYStringBuilder} with the correct formatting settings. Implementations of
     * {@link AbstractOSMSubcommand} should use this method instead of instantiating their own
     * string builders.
     *
     * @return the string builder
     */
    protected TTYStringBuilder getTTYStringBuilder()
    {
        return new TTYStringBuilder(this.useColor);
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
     * Prints the supplied message like "commandName: message" to stderr. Automatically appends a
     * newline to the output.
     *
     * @param message
     *            the message
     */
    protected void printlnCommandMessage(final String message)
    {
        printStderr(this.getCommandName() + ": ");
        printStderr(message + "\n");
    }

    /**
     * Prints the supplied message like "commandName: error: message" with automatic coloring to
     * stderr. Automatically appends a newline to the output.
     *
     * @param message
     *            the error message
     */
    protected void printlnErrorMessage(final String message)
    {
        printStderr(this.getCommandName() + ": ");
        printStderr("error: ", TTYAttribute.BOLD, TTYAttribute.RED);
        printStderr(message + "\n");
    }

    /**
     * Prints the supplied message like "commandName: warn: message" with automatic coloring to
     * stderr. Automatically appends a newline to the output.
     *
     * @param message
     *            the warn message
     */
    protected void printlnWarnMessage(final String message)
    {
        printStderr(this.getCommandName() + ": ");
        printStderr("warn: ", TTYAttribute.BOLD, TTYAttribute.MAGENTA);
        printStderr(message + "\n");
    }

    /**
     * Print a message to STDERR with the supplied attributes.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    protected void printStderr(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilder();
        builder.append(string, attributes);
        System.err.print(builder.toString());
    }

    /**
     * Print a message to STDOUT with the supplied attributes.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    protected void printStdout(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilder();
        builder.append(string, attributes);
        System.out.print(builder.toString());
    }

    /**
     * Print a message to STDERR with the supplied attributes, but only if the command was run using
     * the '--verbose' flag.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    protected void printVerboseStderr(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilder();
        builder.append(string, attributes);
        if (hasOption(DEFAULT_VERBOSE_LONG))
        {
            System.err.print(builder.toString());
        }
    }

    /**
     * Print a message to STDERR with the supplied attributes, but only if the command was run using
     * the '--verbose' flag.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    protected void printVerboseStdout(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilder();
        builder.append(string, attributes);
        if (hasOption(DEFAULT_VERBOSE_LONG))
        {
            System.out.print(builder.toString());
        }
    }

    /**
     * Register an argument with a given arity. The argument hint is used as a key to retrieve the
     * argument value(s) later. Additionally, documentation can use the hint to specify what the
     * argument should be for.
     *
     * @param argumentHint
     *            the hint for the argument
     * @param arity
     *            the argument arity
     * @param type
     *            whether the argument is optional or required
     * @throws CoreException
     *             if the argument could not be registered
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
     * @throws CoreException
     *             if the option could not be registered
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
     * @throws CoreException
     *             if the option could not be registered
     */
    protected void registerOption(final String longForm, final String description)
    {
        this.parser.registerOption(longForm, description);
    }

    /**
     * Register an option with a given long and short form that takes an optional argument. The
     * provided argument hint can be used for generated documentation, and should be a single word
     * describing the argument. The parser will throw an exception at parse-time if the argument is
     * not supplied.
     *
     * @param longForm
     *            the long form of the option, eg. --option
     * @param shortForm
     *            the short form of the option, eg. -o
     * @param description
     *            a simple description
     * @param argumentHint
     *            the hint for the argument
     * @throws CoreException
     *             if the option could not be registered
     */
    protected void registerOptionWithOptionalArgument(final String longForm,
            final Character shortForm, final String description, final String argumentHint)
    {
        this.parser.registerOptionWithOptionalArgument(longForm, shortForm, description,
                argumentHint);
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
     * @throws CoreException
     *             if the option could not be registered
     */
    protected void registerOptionWithOptionalArgument(final String longForm,
            final String description, final String argumentHint)
    {
        this.parser.registerOptionWithOptionalArgument(longForm, description, argumentHint);
    }

    /**
     * Register an option with a given long form that takes a required argument. The provided
     * argument hint can be used for generated documentation, and should be a single word describing
     * the argument. The parser will throw an exception at parse-time if the argument is not
     * supplied.
     *
     * @param longForm
     *            the long form of the option, eg. --option
     * @param shortForm
     *            the short form of the option, eg. -o
     * @param description
     *            a simple description
     * @param argumentHint
     *            the hint for the argument
     * @throws CoreException
     *             if the option could not be registered
     */
    protected void registerOptionWithRequiredArgument(final String longForm,
            final Character shortForm, final String description, final String argumentHint)
    {
        this.parser.registerOptionWithRequiredArgument(longForm, shortForm, description,
                argumentHint);
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
     * @throws CoreException
     *             if the option could not be registered
     */
    protected void registerOptionWithRequiredArgument(final String longForm,
            final String description, final String argumentHint)
    {
        this.parser.registerOptionWithRequiredArgument(longForm, description, argumentHint);
    }

    /**
     * Run this subcommand using all the special setup and teardown semantics provided by
     * {@link AbstractOSMSubcommand}. It automatically registers some default standard arguments:
     * (help,h) and (verbose,v). An example of how this method should be called to make the command
     * functional with an external wrapper:
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
    protected void runSubcommandAndExit(final String... args)
    {
        this.parser.registerOption(DEFAULT_HELP_LONG, DEFAULT_HELP_SHORT, "Show this help menu.");
        this.parser.registerOption(DEFAULT_VERBOSE_LONG, DEFAULT_VERBOSE_SHORT,
                "Use verbose output.");

        String[] argsCopy = args;

        // check the last arg to see if we should disable colors
        if (argsCopy.length > 0 && NO_COLOR_OPTION.equals(argsCopy[argsCopy.length - 1]))
        {
            this.useColor = false;
            argsCopy = Arrays.copyOf(argsCopy, argsCopy.length - 1);
        }

        // fill out appropriate data structures so the execute() implementation can query
        registerOptionsAndArguments();

        // Special case if user supplied '--help' or '-h'
        // We want to scan now, show the help menu, then abort
        if (this.parser.scanForHelpFlag(Arrays.asList(argsCopy)))
        {
            System.out.println(this.getHelpMenu());
            System.exit(0);
        }

        // Special case if user supplied '--version' or '-V'
        // We want to scan now, show the version, then abort
        if (this.parser.scanForVersionFlag(Arrays.asList(argsCopy)))
        {
            System.out.println(String.format("%s version %s", getCommandName(), this.version));
            System.exit(0);
        }

        // parse the options and arguments, throwing exceptions on bad input
        try
        {
            this.parser.parseOptionsAndArguments(Arrays.asList(argsCopy));
        }
        catch (final UnknownOptionException exception)
        {
            printlnErrorMessage("unknown option \'" + exception.getMessage() + "\'");
            printStderr("Try \'");
            printStderr("--help", TTYAttribute.BOLD);
            printStderr("\' option for more info.\n");
            System.exit(1);
        }
        catch (final OptionParseException exception)
        {
            printlnErrorMessage(exception.getMessage());
            printStderr("Try \'");
            printStderr("--help", TTYAttribute.BOLD);
            printStderr("\' option for more info.\n");
            System.exit(1);
        }
        catch (final ArgumentException exception)
        {
            printlnErrorMessage(exception.getMessage());
            printStderr("Try \'");
            printStderr("--help", TTYAttribute.BOLD);
            printStderr("\' option for more info.\n");
            System.exit(1);
        }

        // run the command
        System.exit(execute());
    }

    /**
     * Set the version of this command. Also automatically registers a version option with forms
     * "--version" and "-V".
     *
     * @param version
     *            the version string to use (eg. 1.0.0)
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
        final TTYStringBuilder builder = getTTYStringBuilder();
        builder.append("\n");
        builder.append("NAME", TTYAttribute.BOLD);
        builder.append("\n\t" + name + " -- " + simpleDescription + "\n");
        builder.append("\n");
        return builder.toString();
    }
}

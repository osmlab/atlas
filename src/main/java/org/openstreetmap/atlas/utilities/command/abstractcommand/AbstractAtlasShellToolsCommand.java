package org.openstreetmap.atlas.utilities.command.abstractcommand;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.documentation.DocumentationFormatter;
import org.openstreetmap.atlas.utilities.command.documentation.DocumentationRegistrar;
import org.openstreetmap.atlas.utilities.command.documentation.PagerHelper;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.SimpleOption;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.AmbiguousAbbreviationException;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.UnknownOptionException;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.UnparsableContextException;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;
import org.openstreetmap.atlas.utilities.command.terminal.TTYStringBuilder;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A partial implementation of an Atlas Shell Tools command. Contains significant functionality to
 * aid in command development, including some builtin options.
 *
 * @author lcram
 * @author bbreithaupt
 */
public abstract class AbstractAtlasShellToolsCommand implements AtlasShellToolsMarkerInterface
{
    /**
     * DEFAULT_CONTEXT is the integer value of a command's default context. Subclasses may register
     * additional option parser contexts. They should use this value when performing contextual
     * operations on the default context. See {@link SimpleOptionAndArgumentParser} for more
     * information about contexts.
     */
    public static final int DEFAULT_CONTEXT = 3;

    private static final String LINE_SEPARATOR = "line.separator";
    private static final Logger logger = LoggerFactory
            .getLogger(AbstractAtlasShellToolsCommand.class);

    /**
     * Until Java supports the ability to do granular TTY configuration checking thru an interface
     * like isatty(3), we must rely on special tail arguments. An external wrapper (bash, perl,
     * etc.) can do the necessary TTY config check, and then pass these sentinels to tell the
     * subcommand if it should use special formatting. A ticket to support better TTY in Java
     * checking has been open for years, with no avail:
     * https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4099017. In addition to the tail
     * arguments declared here, this command also expects a TTY maximum column value (a single
     * integer). See {@link AbstractAtlasShellToolsCommand#runSubcommandAndExit(String...)} for the
     * code that unpacks the tail arguments.
     */
    private static final String JAVA_COLOR_STDOUT = "___atlas-shell-tools_color_stdout_SPECIALARGUMENT___";
    private static final String JAVA_NO_COLOR_STDOUT = "___atlas-shell-tools_nocolor_stdout_SPECIALARGUMENT___";
    private static final String JAVA_COLOR_STDERR = "___atlas-shell-tools_color_stderr_SPECIALARGUMENT___";
    private static final String JAVA_NO_COLOR_STDERR = "___atlas-shell-tools_nocolor_stderr_SPECIALARGUMENT___";
    private static final String JAVA_USE_PAGER = "___atlas-shell-tools_use_pager_SPECIALARGUMENT___";
    private static final String JAVA_NO_USE_PAGER = "___atlas-shell-tools_no_use_pager_SPECIALARGUMENT___";
    private static final String JAVA_MARKER_SENTINEL = "___atlas-shell-tools_LAST_ARG_MARKER_SENTINEL___";
    private static final int NUMBER_SENTINELS = 5;
    private static final int STDOUT_COLOR_OFFSET = 5;
    private static final int STDERR_COLOR_OFFSET = 4;
    private static final int PAGER_OFFSET = 3;
    private static final int TERMINAL_COLUMN_OFFSET = 2;

    /*
     * The following options are default-registered.
     */
    private static final String VERBOSE_OPTION_LONG = "verbose";
    private static final Character VERBOSE_OPTION_SHORT = 'v';
    private static final String VERBOSE_OPTION_DESCRIPTION = "Show verbose output messages.";

    private static final String HELP_OPTION_LONG = "help";
    private static final Character HELP_OPTION_SHORT = 'h';
    private static final String HELP_OPTION_DESCRIPTION = "Show this help menu.";

    private static final String VERSION_OPTION_LONG = "version";
    private static final Character VERSION_OPTION_SHORT = 'V';
    private static final String VERSION_OPTION_DESCRIPTION = "Print the command version and exit.";

    private static final int HELP_OPTION_CONTEXT = 1;
    private static final int VERSION_OPTION_CONTEXT = 2;

    /*
     * This is a hack option that can be supplied by callers to override the option parser's default
     * behaviour. If present, it is stripped from ARGS before reaching the option parser.
     */
    private static final String IGNORE_UNKNOWN_OPTIONS = "--ignore-unknown-options";

    /*
     * Maximum allowed column width. If the user's terminal is very wide, we don't want to display
     * documentation all the way to the max column, since it may become hard to read.
     */
    private static final int MAXIMUM_ALLOWED_COLUMN = 225;

    private final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
    private final DocumentationRegistrar registrar = new DocumentationRegistrar();

    /*
     * These variables control terminal-related parameters. Their values can be set by the caller
     * from the command line using sentinal arguments. Try running the atlas-shell-tools "atlas"
     * wrapper perl program with the '--debug' option to see how this works. Also, see the method
     * "unpackSentinelArguments".
     */
    private boolean useColorStdout = false;
    private boolean useColorStderr = false;
    private boolean usePager = false;
    private int maximumColumn = DocumentationFormatter.DEFAULT_MAXIMUM_COLUMN;
    private String version = "default_version_value";
    private boolean ignoreUnknownOptions = false;

    /*
     * Why are we using PrintStreams instead of a logger? Well, we aren't totally. We still
     * encourage the use of a logger with various levels for diagnostic messages (see the many
     * examples of this in subclass implementations). However, for simplicity's sake we use
     * System.out/err for direct messages intended for the user to read on the command line. This
     * makes it easier for users to adjust their log configuration to show the desired level of
     * diagnostics without affecting the actual command outputs. We are declaring explicit stream
     * variables here so that client classes (i.e. unit tests) can override the streams for testing
     * purposes.
     */
    /**
     * See {@link AbstractAtlasShellToolsCommand#setNewOutStream(PrintStream)}.
     */
    private PrintStream outStream = System.out; // NOSONAR
    /**
     * See {@link AbstractAtlasShellToolsCommand#setNewErrStream(PrintStream)}.
     */
    private PrintStream errStream = System.err; // NOSONAR
    /**
     * See {@link AbstractAtlasShellToolsCommand#setNewInStream(InputStream)}.
     */
    private InputStream inStream = System.in;
    /**
     * The default value here is {@link FileSystems#getDefault()}. See
     * {@link AbstractAtlasShellToolsCommand#setNewFileSystem(FileSystem)} for more information.
     */
    private FileSystem fileSystem = FileSystems.getDefault();
    /**
     * By default the command environment will be given by {@link System#getenv()}. See
     * {@link AbstractAtlasShellToolsCommand#setNewEnvironment(Map)} for more information.
     */
    private Map<String, String> environment = null;

    /**
     * Add a section to this command's manual page. The section name will be made all capitalized.
     * Also, use the supplied input stream to read contents into the section.
     *
     * @param section
     *            the name of the section
     * @param sectionResourceFileStream
     *            an input stream to the section resource file (easily specified like
     *            CommandName.class.getResourceAsStream("resourcefile.txt"))
     */
    public void addManualPageSection(final String section,
            final InputStream sectionResourceFileStream)
    {
        this.registrar.addManualPageSection(section, sectionResourceFileStream);
    }

    /**
     * Execute the command logic. Subclasses of {@link AbstractAtlasShellToolsCommand} must
     * implement this method, but in general it should not be called directly. See
     * {@link AbstractAtlasShellToolsCommand#runSubcommandAndExit(String...)}.
     *
     * @return the return code of the command
     */
    public abstract int execute();

    /**
     * Force the current command to exit with a given exit code.
     *
     * @param exitCode
     *            the exit code
     */
    public void forceExit(final int exitCode)
    {
        System.exit(exitCode);
    }

    /**
     * The simple name of the command. This should be easy to type for ease of command line use.
     *
     * @return the simple name of the command
     */
    public abstract String getCommandName();

    /**
     * Get a {@link CommandOutputDelegate} bound to this {@link AbstractAtlasShellToolsCommand}.
     *
     * @return a delegate bound to this command
     */
    public CommandOutputDelegate getCommandOutputDelegate()
    {
        return new CommandOutputDelegate(this);
    }

    /**
     * Get the value of an environment variable. Command implementations should always defer to this
     * method rather than {@link System#getenv()}, since unit tests may be utilizing
     * {@link AbstractAtlasShellToolsCommand#setNewEnvironment(Map)} to inject a custom environment
     * for testing purposes.
     *
     * @param name
     *            the name of the environment variable
     * @return the string value of the variable, or {@code null} if the variable is not defined in
     *         the environment
     */
    public String getEnvironmentValue(final String name)
    {
        if (this.environment == null)
        {
            return System.getenv(name);
        }
        return this.environment.getOrDefault(name, null);
    }

    /**
     * Get the {@link PrintStream} for this command's err stream.
     *
     * @return the {@link PrintStream} for this command's err stream.
     */
    public PrintStream getErrStream()
    {
        return this.errStream;
    }

    /**
     * Get the {@link FileSystem} for this command.
     *
     * @return the {@link FileSystem} for this command.
     */
    public FileSystem getFileSystem()
    {
        return this.fileSystem;
    }

    /**
     * Get the {@link InputStream} for this command.
     *
     * @return the {@link InputStream} for this command.
     */
    public InputStream getInStream()
    {
        return this.inStream;
    }

    /**
     * Get the maximum column of the current terminal.
     *
     * @return the maximum column
     */
    public int getMaximumColumn()
    {
        return this.maximumColumn;
    }

    /**
     * Get an {@link OptionAndArgumentDelegate} bound to this
     * {@link AbstractAtlasShellToolsCommand}.
     *
     * @return a fetcher bound to this command
     */
    public OptionAndArgumentDelegate getOptionAndArgumentDelegate()
    {
        return new OptionAndArgumentDelegate(this);
    }

    /**
     * Get the {@link PrintStream} for this command's out stream.
     *
     * @return the {@link PrintStream} for this command's out stream.
     */
    public PrintStream getOutStream()
    {
        return this.outStream;
    }

    /**
     * A simple description of the command. It should be brief - see the NAME section of any man
     * page for an example.
     *
     * @return the description
     */
    public abstract String getSimpleDescription();

    /**
     * Get a {@link TTYStringBuilder} that is configured to respect the color settings of stderr.
     *
     * @return the configured {@link TTYStringBuilder}
     */
    public TTYStringBuilder getTTYStringBuilderForStderr()
    {
        return new TTYStringBuilder(this.useColorStderr);
    }

    /**
     * Get a {@link TTYStringBuilder} that is configured to respect the color settings of stdout.
     *
     * @return the configured {@link TTYStringBuilder}
     */
    public TTYStringBuilder getTTYStringBuilderForStdout()
    {
        return new TTYStringBuilder(this.useColorStdout);
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
     * @param optionality
     *            whether the argument is optional or required
     * @param contexts
     *            the contexts
     * @throws CoreException
     *             if the argument could not be registered
     */
    public void registerArgument(final String argumentHint, final ArgumentArity arity,
            final ArgumentOptionality optionality, final Integer... contexts)
    {
        if (contexts.length == 0)
        {
            this.parser.registerArgument(argumentHint, arity, optionality, DEFAULT_CONTEXT);
        }
        else
        {
            this.parser.registerArgument(argumentHint, arity, optionality, contexts);
        }
    }

    /**
     * Register an empty context. This is useful if you want to have a defined usage case where no
     * options or arguments are passed.
     *
     * @param context
     *            the context id
     */
    public void registerEmptyContext(final int context)
    {
        this.parser.registerEmptyContext(context);
    }

    /**
     * Register any desired manual page sections. An OPTIONS section will be automatically
     * generated, so it is recommended that you register at least a DESCRIPTION and EXAMPLES section
     * with some appropriate documentation. See other {@link AbstractAtlasShellToolsCommand}
     * implementations for how this is done. For clarification on best practices and/or other
     * sections to include, see any system man-page (git(1), curl(1), and less(1) are good places to
     * start).
     */
    public abstract void registerManualPageSections();

    /**
     * Register a {@link AtlasShellToolsCommandTemplate}'s manual pages for this command.
     *
     * @param template
     *            the {@link AtlasShellToolsCommandTemplate} whose man pages sections you want to
     *            register
     */
    public void registerManualPageSectionsFromTemplate(
            final AtlasShellToolsCommandTemplate template)
    {
        template.registerManualPageSections(this);
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
     * @param optionality
     *            the optionality
     * @param contexts
     *            the contexts
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOption(final String longForm, final Character shortForm,
            final String description, final OptionOptionality optionality,
            final Integer... contexts)
    {
        if (contexts.length == 0)
        {
            this.parser.registerOption(longForm, shortForm, description, optionality,
                    DEFAULT_CONTEXT);
        }
        else
        {
            this.parser.registerOption(longForm, shortForm, description, optionality, contexts);
        }
    }

    /**
     * Register an option with a given long form. The option will be a flag option, ie. it can take
     * no arguments.
     *
     * @param longForm
     *            the long form of the option, eg. --option
     * @param description
     *            a simple description
     * @param optionality
     *            the optionality
     * @param contexts
     *            the contexts
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOption(final String longForm, final String description,
            final OptionOptionality optionality, final Integer... contexts)
    {
        if (contexts.length == 0)
        {
            this.parser.registerOption(longForm, description, optionality, DEFAULT_CONTEXT);
        }
        else
        {
            this.parser.registerOption(longForm, description, optionality, contexts);
        }
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
     * @param optionality
     *            the optionality
     * @param argumentHint
     *            the hint for the argument
     * @param contexts
     *            the contexts
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOptionWithOptionalArgument(final String longForm, final Character shortForm,
            final String description, final OptionOptionality optionality,
            final String argumentHint, final Integer... contexts)
    {
        if (contexts.length == 0)
        {
            this.parser.registerOptionWithOptionalArgument(longForm, shortForm, description,
                    optionality, argumentHint, DEFAULT_CONTEXT);
        }
        else
        {
            this.parser.registerOptionWithOptionalArgument(longForm, shortForm, description,
                    optionality, argumentHint, contexts);
        }
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
     * @param optionality
     *            the optionality
     * @param argumentHint
     *            the hint for the argument
     * @param contexts
     *            the contexts
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOptionWithOptionalArgument(final String longForm, final String description,
            final OptionOptionality optionality, final String argumentHint,
            final Integer... contexts)
    {
        if (contexts.length == 0)
        {
            this.parser.registerOptionWithOptionalArgument(longForm, description, optionality,
                    argumentHint, DEFAULT_CONTEXT);
        }
        else
        {
            this.parser.registerOptionWithOptionalArgument(longForm, description, optionality,
                    argumentHint, contexts);
        }
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
     * @param optionality
     *            the optionality
     * @param argumentHint
     *            the hint for the argument
     * @param contexts
     *            the contexts
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOptionWithRequiredArgument(final String longForm, final Character shortForm,
            final String description, final OptionOptionality optionality,
            final String argumentHint, final Integer... contexts)
    {
        if (contexts.length == 0)
        {
            this.parser.registerOptionWithRequiredArgument(longForm, shortForm, description,
                    optionality, argumentHint, DEFAULT_CONTEXT);
        }
        else
        {
            this.parser.registerOptionWithRequiredArgument(longForm, shortForm, description,
                    optionality, argumentHint, contexts);
        }
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
     * @param optionality
     *            the optionality
     * @param argumentHint
     *            the hint for the argument
     * @param contexts
     *            the contexts
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOptionWithRequiredArgument(final String longForm, final String description,
            final OptionOptionality optionality, final String argumentHint,
            final Integer... contexts)
    {
        if (contexts.length == 0)
        {
            this.parser.registerOptionWithRequiredArgument(longForm, description, optionality,
                    argumentHint, DEFAULT_CONTEXT);
        }
        else
        {
            this.parser.registerOptionWithRequiredArgument(longForm, description, optionality,
                    argumentHint, contexts);
        }
    }

    /**
     * Register any necessary options and arguments for the command. Subclasses should override this
     * method, but call super.registerOptionsAndArguments last in order to pick up super class
     * options/args.
     */
    public void registerOptionsAndArguments()
    {
        // register --help and --version to contexts 1 and 2, respectively
        registerOption(HELP_OPTION_LONG, HELP_OPTION_SHORT, HELP_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, HELP_OPTION_CONTEXT);
        registerOption(VERSION_OPTION_LONG, VERSION_OPTION_SHORT, VERSION_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, VERSION_OPTION_CONTEXT);
        registerEmptyContext(DEFAULT_CONTEXT);

        // register a default '--verbose' option in all contexts (except the --help and --version)
        final Integer[] contexts = this.getFilteredRegisteredContexts().toArray(new Integer[0]);
        registerOption(VERBOSE_OPTION_LONG, VERBOSE_OPTION_SHORT, VERBOSE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, contexts);
    }

    /**
     * Register a {@link AtlasShellToolsCommandTemplate}'s options and arguments for this command.
     *
     * @param template
     *            the {@link AtlasShellToolsCommandTemplate} whose options and arguments you want to
     *            register
     */
    public void registerOptionsAndArgumentsFromTemplate(
            final AtlasShellToolsCommandTemplate template)
    {
        template.registerOptionsAndArguments(this);
    }

    /**
     * Run this subcommand using all the special setup and teardown semantics provided by
     * {@link AbstractAtlasShellToolsCommand}. It automatically registers some default standard
     * arguments: (help,h) and (verbose,v).
     *
     * @param args
     *            the command arguments
     * @return an {@code int} that can be used as a system return code
     */
    public int runSubcommand(final String... args)
    {
        throwIfInvalidNameOrDescription();

        final String[] argsCopy = unpackSentinelArguments(args);
        this.parser.ignoreUnknownOptions(this.ignoreUnknownOptions);

        // fill out appropriate data structures so the execute() implementation can query
        registerOptionsAndArguments();
        registerManualPageSections();

        // parse the options and arguments, throwing exceptions on bad input
        try
        {
            this.parser.parse(Arrays.asList(argsCopy));
        }
        catch (final AmbiguousAbbreviationException | UnknownOptionException
                | UnparsableContextException exception)
        {
            printlnErrorMessage(exception.getMessage());
            printSimpleUsageMenu();
            printStderr("Try the \'");
            printStderr("--help", TTYAttribute.BOLD);
            printStderr("\' option (e.g. ");
            printStderr("atlas " + this.getCommandName() + " --help", TTYAttribute.BOLD);
            printlnStderr(") for more info");
            return 1;
        }
        catch (final Exception exception)
        {
            throw new CoreException("unhandled exception", exception);
        }

        logger.debug("Command using context {}", this.parser.getContext());

        // handle the hardcoded --help and --version options
        if (this.parser.hasOption(HELP_OPTION_LONG))
        {
            if (this.usePager)
            {
                final PagerHelper helper = new PagerHelper();
                helper.pageString(this.getHelpMenu());
            }
            else
            {
                printlnStdout(this.getHelpMenu());
            }
            return 0;
        }

        if (this.parser.hasOption(VERSION_OPTION_LONG))
        {
            printlnStdout(String.format("%s version %s", getCommandName(), this.version));
            return 0;
        }

        // run the command
        return execute();
    }

    /**
     * Run this subcommand and exit the JVM. An example of how this method should be called to make
     * the command functional with an external wrapper:
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
    public void runSubcommandAndExit(final String... args)
    {
        System.exit(this.runSubcommand(args));
    }

    /**
     * Set a new {@link Map} to act as the system environment for this command. This may be useful
     * for various unit tests which want to inject alternate values into a command's environment
     * variables (for example, changing the location of user.home to be independent of the machine
     * context).
     *
     * @param newEnvironment
     *            the new environment {@link Map}
     */
    public void setNewEnvironment(final Map<String, String> newEnvironment)
    {
        this.environment = newEnvironment;
    }

    /**
     * Set a new {@link PrintStream} for the stderr writer methods. This may be useful for various
     * unit tests which want to intercept command error output to check it against some expected
     * result.
     *
     * @param newErrStream
     *            the new err {@link PrintStream}
     */
    public void setNewErrStream(final PrintStream newErrStream)
    {
        this.errStream = newErrStream;
    }

    /**
     * Set a new {@link FileSystem} for this command. Implementations should respect the set
     * {@link FileSystem} when performing file operations. This is particularly useful for
     * unit-testing, where we may want to use an alternate file system (e.g. in-memory).
     *
     * @param newFileSystem
     *            the new {@link FileSystem} to use.
     */
    public void setNewFileSystem(final FileSystem newFileSystem)
    {
        this.fileSystem = newFileSystem;
    }

    /**
     * Set a new {@link InputStream} for this command. Implementations should respect the set
     * {@link InputStream} when reading input from the user. This is particularly useful for
     * unit-testing, where we may want to inject arbitrary input for testing purposes.
     *
     * @param inStream
     *            the new {@link InputStream} to use
     */
    public void setNewInStream(final InputStream inStream)
    {
        this.inStream = inStream;
    }

    /**
     * Set a new {@link PrintStream} for the stdout writer methods. This may be useful for various
     * unit tests which want to intercept command output to check it against some expected result.
     *
     * @param newOutStream
     *            the new out {@link PrintStream}
     */
    public void setNewOutStream(final PrintStream newOutStream)
    {
        this.outStream = newOutStream;
    }

    /**
     * Check that the command name and description are valid. This should be called before relying
     * on the return values of getCommandName and getDescription.
     */
    public void throwIfInvalidNameOrDescription()
    {
        final String name = this.getCommandName();
        if (name == null || name.isEmpty())
        {
            throw new CoreException("{} command name must not be null or empty",
                    this.getClass().getName());
        }
        final String[] split = name.split("\\s+");
        if (split.length > 1)
        {
            throw new CoreException("{} command name must not contain whitespace",
                    this.getClass().getName());
        }
        for (int index = 0; index < name.length(); index++)
        {
            final char currentCharacter = name.charAt(index);
            if (!Character.isLetterOrDigit(currentCharacter) && currentCharacter != '-'
                    && currentCharacter != '_')
            {
                throw new CoreException(
                        "{} command name must only contain letters, digits, hyphens, or underscores",
                        this.getClass().getName());
            }
        }

        final String simpleDescription = this.getSimpleDescription();
        if (simpleDescription == null || simpleDescription.isEmpty())
        {
            throw new CoreException("{} simple description must not be null or empty",
                    this.getClass().getName());
        }
    }

    /**
     * Add a given code line to a given manual page section. Code lines are given additional
     * indentation and are excluded from line-wrap formatting. If a code line contains a newline,
     * the formatting will not automatically indent after the line break.
     *
     * @param section
     *            the section to add to
     * @param codeLine
     *            the code line
     * @throws CoreException
     *             if the section does not exist
     */
    protected void addCodeLineToSection(final String section, final String codeLine)
    {
        this.registrar.addCodeLineToSection(section, codeLine);
    }

    /**
     * Add a section to this command's manual page. The section name will be made all capitalized.
     *
     * @param section
     *            the name of the section
     */
    protected void addManualPageSection(final String section)
    {
        this.registrar.addManualPageSection(section);
    }

    /**
     * Add a given paragraph to a given manual page section.
     *
     * @param section
     *            the section to add to
     * @param paragraph
     *            the paragraph
     * @throws CoreException
     *             if the section does not exist
     */
    protected void addParagraphToSection(final String section, final String paragraph)
    {
        this.registrar.addParagraphToSection(section, paragraph);
    }

    /**
     * Set the version of this command.
     *
     * @param version
     *            the version string to use (eg. 1.0.0)
     */
    protected void setVersion(final String version)
    {
        this.version = version;
    }

    SortedSet<Integer> getFilteredRegisteredContexts()
    {
        // filter out the default, hardcoded '--help' and '--version' contexts
        final Set<Integer> set = this.parser.getRegisteredContexts().stream().filter(
                context -> context != HELP_OPTION_CONTEXT && context != VERSION_OPTION_CONTEXT)
                .collect(Collectors.toSet());

        return new TreeSet<>(set);
    }

    Optional<String> getOptionArgument(final String longForm)
    {
        return this.parser.getOptionArgument(longForm);
    }

    <T> Optional<T> getOptionArgument(final String longForm, final StringConverter<T> converter)
    {
        return this.parser.getOptionArgument(longForm, converter);
    }

    int getParserContext()
    {
        return this.parser.getContext();
    }

    Optional<String> getUnaryArgument(final String hint)
    {
        return this.parser.getUnaryArgument(hint);
    }

    List<String> getVariadicArgument(final String hint)
    {
        return this.parser.getVariadicArgument(hint);
    }

    boolean hasOption(final String longForm)
    {
        return this.parser.hasOption(longForm);
    }

    boolean hasVerboseOption()
    {
        return this.parser.hasOption(VERBOSE_OPTION_LONG);
    }

    void printStderr(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilderForStderr();
        builder.append(string, attributes);
        this.errStream.print(builder.toString()); // NOSONAR
    }

    void printStdout(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilderForStdout();
        builder.append(string, attributes);
        this.outStream.print(builder.toString()); // NOSONAR
    }

    void printlnCommandMessage(final String message)
    {
        printStderr(this.getCommandName() + ": ");
        printStderr(message + System.getProperty(LINE_SEPARATOR));
    }

    void printlnErrorMessage(final String message)
    {
        printStderr(this.getCommandName() + ": ");
        printStderr("error: ", TTYAttribute.BOLD, TTYAttribute.RED);
        printStderr(message + System.getProperty(LINE_SEPARATOR));
    }

    void printlnStderr(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilderForStderr();
        builder.append(string, attributes);
        this.errStream.println(builder.toString()); // NOSONAR
    }

    void printlnStdout(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilderForStdout();
        builder.append(string, attributes);
        this.outStream.println(builder.toString()); // NOSONAR
    }

    void printlnWarnMessage(final String message)
    {
        printStderr(this.getCommandName() + ": ");
        printStderr("warn: ", TTYAttribute.BOLD, TTYAttribute.MAGENTA);
        printStderr(message + System.getProperty(LINE_SEPARATOR));
    }

    private String getHelpMenu()
    {
        final String name = this.getCommandName();
        final String simpleDescription = this.getSimpleDescription();
        final Map<Integer, Set<SimpleOption>> optionsWithContext = this.parser
                .getContextToRegisteredOptions();
        final Set<SimpleOption> allOptions = this.parser.getRegisteredOptions();
        final TTYStringBuilder builder = getTTYStringBuilderForStdout();

        builder.newline();

        DocumentationFormatter.generateTextForNameSection(name, simpleDescription, builder);
        builder.newline();

        DocumentationFormatter.generateTextForSynopsisSection(name, this.maximumColumn,
                optionsWithContext, this.parser.getRegisteredContexts(),
                this.parser.getArgumentHintToArity(), this.parser.getArgumentHintToOptionality(),
                builder);
        builder.newline();

        // Let's manually insert the DESCRIPTION section first, if it exists.
        // This is typical for manpages, DESCRIPTION always comes before OPTIONS.
        if (this.registrar.hasDescriptionSection())
        {
            DocumentationFormatter.generateTextForGenericSection(
                    this.registrar.getDescriptionHeader(), this.maximumColumn, builder,
                    this.registrar);
            builder.newline();
        }

        DocumentationFormatter.generateTextForOptionsSection(this.maximumColumn, allOptions,
                builder);
        builder.newline();

        // Insert the rest of the user designed sections
        for (final String section : this.registrar.getSections())
        {
            // Skip DESCRIPTION header, since we already inserted it before OPTIONS
            if (this.registrar.getDescriptionHeader().equals(section))
            {
                continue;
            }
            DocumentationFormatter.generateTextForGenericSection(section, this.maximumColumn,
                    builder, this.registrar);
            builder.newline();
        }

        return builder.toString();
    }

    private void printSimpleUsageMenu()
    {
        final String name = this.getCommandName();
        final Map<Integer, Set<SimpleOption>> optionsWithContext = this.parser
                .getContextToRegisteredOptions();
        final TTYStringBuilder builder = getTTYStringBuilderForStderr();

        DocumentationFormatter.generateTextForSynopsisSection(name, this.maximumColumn,
                optionsWithContext, this.parser.getRegisteredContexts(),
                this.parser.getArgumentHintToArity(), this.parser.getArgumentHintToOptionality(),
                builder);

        printlnStderr(builder.toString());
    }

    private String[] unpackSentinelArguments(final String[] args)
    {
        List<String> argsAsList = Arrays.asList(args);
        argsAsList = new ArrayList<>(argsAsList);
        if (!argsAsList.isEmpty() && argsAsList.contains(IGNORE_UNKNOWN_OPTIONS))
        {
            this.ignoreUnknownOptions = true;
            argsAsList.remove(IGNORE_UNKNOWN_OPTIONS);
        }
        if (!argsAsList.isEmpty()
                && JAVA_MARKER_SENTINEL.equals(argsAsList.get(argsAsList.size() - 1)))
        {
            final String stdoutColorArg = argsAsList.get(argsAsList.size() - STDOUT_COLOR_OFFSET);
            final String stderrColorArg = argsAsList.get(argsAsList.size() - STDERR_COLOR_OFFSET);
            final String usePagerArg = argsAsList.get(argsAsList.size() - PAGER_OFFSET);
            final String terminalColumnArg = argsAsList
                    .get(argsAsList.size() - TERMINAL_COLUMN_OFFSET);
            if (JAVA_COLOR_STDOUT.equals(stdoutColorArg))
            {
                this.useColorStdout = true;
            }
            else if (JAVA_NO_COLOR_STDOUT.equals(stdoutColorArg))
            {
                this.useColorStdout = false;
            }
            if (JAVA_COLOR_STDERR.equals(stderrColorArg))
            {
                this.useColorStderr = true;
            }
            else if (JAVA_NO_COLOR_STDERR.equals(stderrColorArg))
            {
                this.useColorStderr = false;
            }
            if (JAVA_USE_PAGER.equals(usePagerArg))
            {
                this.usePager = true;
            }
            else if (JAVA_NO_USE_PAGER.equals(usePagerArg))
            {
                this.usePager = false;
            }
            this.maximumColumn = Integer.parseInt(terminalColumnArg);
            if (this.maximumColumn > MAXIMUM_ALLOWED_COLUMN)
            {
                this.maximumColumn = MAXIMUM_ALLOWED_COLUMN;
            }
            argsAsList = argsAsList.subList(0, argsAsList.size() - NUMBER_SENTINELS);
        }
        return argsAsList.toArray(new String[0]);
    }
}

package org.openstreetmap.atlas.utilities.command;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.command.documentation.DocumentationFormatType;
import org.openstreetmap.atlas.utilities.command.documentation.DocumentationFormatter;
import org.openstreetmap.atlas.utilities.command.documentation.DocumentationRegistrar;
import org.openstreetmap.atlas.utilities.command.documentation.PagerHelper;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.AmbiguousAbbreviationException;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.SimpleOption;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.UnknownOptionException;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.UnparsableContextException;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;
import org.openstreetmap.atlas.utilities.command.terminal.TTYStringBuilder;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A partial implementation of an Atlas Shell Tools command. Contains significant functionality to
 * aid in command development, including some builtin options.
 *
 * @author lcram
 */
public abstract class AbstractAtlasShellToolsCommand implements AtlasShellToolsMarker
{
    private static final Logger logger = LoggerFactory
            .getLogger(AbstractAtlasShellToolsCommand.class);

    /*
     * Until Java supports the ability to do granular TTY configuration checking thru an interface
     * like isatty(3), we must rely on special tail arguments. An external wrapper (bash, perl,
     * etc.) can do the necessary TTY config check, and then pass these sentinels to tell the
     * subcommand if it should use special formatting. A ticket to support better TTY in Java
     * checking has been open for years, with no avail:
     * https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4099017
     */
    /**
     * In addition to the tail arguments declared here, this command also expects a TTY maximum
     * column value (a single integer). See
     * {@link AbstractAtlasShellToolsCommand#runSubcommandAndExit(String...)} for the code that
     * unpacks the tail arguments.
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

    private final SimpleOptionAndArgumentParser parser = new SimpleOptionAndArgumentParser();
    private final DocumentationRegistrar registrar = new DocumentationRegistrar();

    private boolean useColorStdout = false;
    private boolean useColorStderr = false;
    private boolean usePager = false;
    private int maximumColumn = DocumentationFormatter.DEFAULT_MAXIMUM_COLUMN;
    private String version = "default_version_value";

    /**
     * Check that the command name and description are valid. This should be called before relying
     * on the return values of getCommandName and getDescription.
     */
    void throwIfInvalidNameOrDescription()
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
     * Execute the command logic. Subclasses of {@link AbstractAtlasShellToolsCommand} must
     * implement this method, but in general it should not be called directly. See
     * {@link AbstractAtlasShellToolsCommand#runSubcommandAndExit(String...)}.
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
     * Register any desired manual page sections. An OPTIONS section will be automatically
     * generated, so it is recommended that you register at least a DESCRIPTION and EXAMPLES section
     * with some appropriate documentation. See other {@link AbstractAtlasShellToolsCommand}
     * implementations for how this is done. For clarification on best practices and/or other
     * sections to include, see any system man-page (git(1), curl(1), and less(1) are good places to
     * start).
     */
    public abstract void registerManualPageSections();

    /*
     * A NOTE ON THE ADAPTER OPTION/ARGUMENT PARSING INTERFACE
     */
    // While this may seem like duplication of the SimpleOptionAndArgumentParser interface,
    // it actually allows us to define an immutable interface for subcommand registration. By
    // setting up the interface this way, we are not wedded to the SimpleOptionAndArgumentParser for
    // future changes. Should we decide to change it, any subcommands extending this class
    // will not have to change their option registration code.

    /**
     * Register any necessary options and arguments for the command. Use the protected API exposed
     * by {@link AbstractAtlasShellToolsCommand}.
     */
    public abstract void registerOptionsAndArguments();

    /**
     * Add a given code block to a given manual page section. Code blocks are given additional
     * indentation and are excluded from line-wrap formatting.
     *
     * @param section
     *            the section to add to
     * @param codeBlock
     *            the code block
     * @throws CoreException
     *             if the section does not exist
     */
    protected void addCodeBlockToSection(final String section, final String codeBlock)
    {
        this.registrar.addCodeBlockToSection(section, codeBlock);
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
     * converter. If the converter function returns null, then this method will return
     * {@link Optional#empty()}.
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

    protected int getParserContext()
    {
        return this.parser.getCurrentContext();
    }

    /**
     * Get a {@link TTYStringBuilder} with the correct formatting settings for stderr.
     * Implementations of {@link AbstractAtlasShellToolsCommand} should use this method instead of
     * instantiating their own string builders.
     *
     * @return the string builder
     */
    protected TTYStringBuilder getTTYStringBuilderForStderr()
    {
        return new TTYStringBuilder(this.useColorStderr);
    }

    /**
     * Get a {@link TTYStringBuilder} with the correct formatting settings for stdout.
     * Implementations of {@link AbstractAtlasShellToolsCommand} should use this method instead of
     * instantiating their own string builders.
     *
     * @return the string builder
     */
    protected TTYStringBuilder getTTYStringBuilderForStdout()
    {
        return new TTYStringBuilder(this.useColorStdout);
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
        printStderr(message + System.getProperty("line.separator"));
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
        printStderr(message + System.getProperty("line.separator"));
    }

    /**
     * Print a message to STDERR with the supplied attributes. Terminates the message with a
     * newline.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    protected void printlnStderr(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilderForStderr();
        builder.append(string, attributes);
        System.err.println(builder.toString());
    }

    /**
     * Print a message to STDOUT with the supplied attributes. Terminates the message with a
     * newline.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    protected void printlnStdout(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilderForStderr();
        builder.append(string, attributes);
        System.out.println(builder.toString());
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
        printStderr(message + System.getProperty("line.separator"));
    }

    /**
     * Print a message (with no ending newline) to STDERR with the supplied attributes.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    protected void printStderr(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilderForStderr();
        builder.append(string, attributes);
        System.err.print(builder.toString());
    }

    /**
     * Print a message (with no ending newline) to STDOUT with the supplied attributes.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    protected void printStdout(final String string, final TTYAttribute... attributes)
    {
        final TTYStringBuilder builder = this.getTTYStringBuilderForStdout();
        builder.append(string, attributes);
        System.out.print(builder.toString());
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
            final ArgumentOptionality type, final Integer... contexts)
    {
        this.parser.registerArgument(argumentHint, arity, type, contexts);
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
            final String description, final Integer... contexts)
    {
        this.parser.registerOption(longForm, shortForm, description, contexts);
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
    protected void registerOption(final String longForm, final String description,
            final Integer... contexts)
    {
        this.parser.registerOption(longForm, description, contexts);
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
            final Character shortForm, final String description, final String argumentHint,
            final Integer... contexts)
    {
        this.parser.registerOptionWithOptionalArgument(longForm, shortForm, description,
                argumentHint, contexts);
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
            final String description, final String argumentHint, final Integer... contexts)
    {
        this.parser.registerOptionWithOptionalArgument(longForm, description, argumentHint,
                contexts);
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
            final Character shortForm, final String description, final String argumentHint,
            final Integer... contexts)
    {
        this.parser.registerOptionWithRequiredArgument(longForm, shortForm, description,
                argumentHint, contexts);
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
            final String description, final String argumentHint, final Integer... contexts)
    {
        this.parser.registerOptionWithRequiredArgument(longForm, description, argumentHint,
                contexts);
    }

    /**
     * Run this subcommand using all the special setup and teardown semantics provided by
     * {@link AbstractAtlasShellToolsCommand}. It automatically registers some default standard
     * arguments: (help,h) and (verbose,v). An example of how this method should be called to make
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
    protected void runSubcommandAndExit(final String... args)
    {
        throwIfInvalidNameOrDescription();

        String[] argsCopy = args;

        // check the last arg to see if we should check for other tail arguments
        if (argsCopy.length > 0 && JAVA_MARKER_SENTINEL.equals(argsCopy[argsCopy.length - 1]))
        {
            final String stdoutColorArg = argsCopy[argsCopy.length - STDOUT_COLOR_OFFSET];
            final String stderrColorArg = argsCopy[argsCopy.length - STDERR_COLOR_OFFSET];
            final String usePagerArg = argsCopy[argsCopy.length - PAGER_OFFSET];
            final String terminalColumnArg = argsCopy[argsCopy.length - TERMINAL_COLUMN_OFFSET];
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
            argsCopy = Arrays.copyOf(argsCopy, argsCopy.length - NUMBER_SENTINELS);
        }

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
            printlnStderr("\' option for more info.");
            System.exit(1);
        }
        catch (final Exception exception)
        {
            throw new CoreException("unhandled exception {}", exception);
        }

        logger.debug("Command using context {}", this.parser.getCurrentContext());

        if (this.parser.getCurrentContext() == SimpleOptionAndArgumentParser.HELP_OPTION_CONTEXT_ID
                && this.parser.hasOption(SimpleOptionAndArgumentParser.DEFAULT_HELP_LONG))
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
            System.exit(0);
        }

        if (this.parser
                .getCurrentContext() == SimpleOptionAndArgumentParser.VERSION_OPTION_CONTEXT_ID
                && this.parser.hasOption(SimpleOptionAndArgumentParser.DEFAULT_VERSION_LONG))
        {
            printlnStdout(String.format("%s version %s", getCommandName(), this.version));
            System.exit(0);
        }

        if (this.parser.isEmpty())
        {
            printlnErrorMessage("command line was empty");
            printSimpleUsageMenu();
            printStderr("Try the \'");
            printStderr("--help", TTYAttribute.BOLD);
            printlnStderr("\' option for more info.");
            System.exit(1);
        }

        // run the command
        System.exit(execute());
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

    private void buildSection(final String section, final TTYStringBuilder builder)
    {
        final List<Tuple<DocumentationFormatType, String>> sectionContents = this.registrar
                .getSectionContents(section);
        builder.append(section, TTYAttribute.BOLD).newline();
        for (final Tuple<DocumentationFormatType, String> contents : sectionContents)
        {
            final DocumentationFormatType type = contents.getFirst();
            final String text = contents.getSecond();
            if (type == DocumentationFormatType.CODE)
            {
                DocumentationFormatter.addCodeBlock(
                        DocumentationFormatter.DEFAULT_CODE_INDENT_LEVEL, text, builder);
            }
            else if (type == DocumentationFormatType.PARAGRAPH)
            {
                DocumentationFormatter.addParagraphWithLineWrapping(
                        DocumentationFormatter.DEFAULT_PARAGRAPH_INDENT_LEVEL, this.maximumColumn,
                        text, builder, true);
            }
            builder.newline().newline();
        }
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

        builder.append("NAME", TTYAttribute.BOLD).newline();
        DocumentationFormatter.indentBuilderToLevel(
                DocumentationFormatter.DEFAULT_PARAGRAPH_INDENT_LEVEL, builder);
        builder.append(name + " -- " + simpleDescription).newline().newline();

        builder.append("SYNOPSIS", TTYAttribute.BOLD).newline();
        DocumentationFormatter.generateTextForSynopsisSection(name, this.maximumColumn,
                optionsWithContext, this.parser.getRegisteredContexts(),
                this.parser.getArgumentHintToArity(), this.parser.getArgumentHintToOptionality(),
                builder);
        builder.newline().newline();

        // Let's manually insert the DESCRIPTION section first, if it exists.
        // This is typical for manpages, DESCRIPTION always comes before OPTIONS.
        if (this.registrar.hasDescriptionSection())
        {
            buildSection(this.registrar.getDescriptionHeader(), builder);
        }

        builder.append("OPTIONS", TTYAttribute.BOLD).newline();
        DocumentationFormatter.generateTextForOptionsSection(this.maximumColumn, allOptions,
                builder);

        // Insert the rest of the user designed sections
        for (final String section : this.registrar.getSections())
        {
            // Skip DESCRIPTION header, since we already inserted it before OPTIONS
            if (this.registrar.getDescriptionHeader().equals(section))
            {
                continue;
            }
            buildSection(section, builder);
        }

        return builder.toString();
    }

    /*
     * TODO refactor
     */
    private void printSimpleUsageMenu()
    {
        final String name = this.getCommandName();
        final Map<Integer, Set<SimpleOption>> optionsWithContext = this.parser
                .getContextToRegisteredOptions();
        final TTYStringBuilder builder = getTTYStringBuilderForStdout();

        builder.append("usage:").newline();
        DocumentationFormatter.generateTextForSynopsisSection(name, this.maximumColumn,
                optionsWithContext, this.parser.getRegisteredContexts(),
                this.parser.getArgumentHintToArity(), this.parser.getArgumentHintToOptionality(),
                builder);
        printlnStderr(builder.toString());
    }
}

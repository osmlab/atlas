package org.openstreetmap.atlas.utilities.command.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple option and argument parser. Designed specifically to impose constraints on the
 * format of the options. Nothing about this class is thread safe, should you decide to parse in one
 * thread and read results in another.
 * <p>
 * Supports long and short options:<br>
 * --opt : a long option<br>
 * --opt-arg=my_argument : a long option with argument, supports optional or required arguments<br>
 * --opt-arg my_argument : alternate syntax for required long option arguments<br>
 * -a : a short option<br>
 * -abc : bundled short options (-a, -b, -c)<br>
 * -o arg : a short option (-o) that takes a required arg<br>
 * -oarg : alternate syntax, a short option (-o) that takes a required or optional arg<br>
 * <br>
 * If an option is specified multiple times with different arguments, the parser will use the
 * version in the highest ARGV position (ie. the furthest right on the command line). Additionally,
 * this class supports easy argument parsing/fetching with unary and variadic arguments.
 * </p>
 * This class supports both the POSIX short option spec as well as the GNU long option spec. See
 * included links for details.
 *
 * @see "https://www.gnu.org/software/libc/manual/html_node/Argument-Syntax.html"
 * @see "http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html"
 * @see "http://pubs.opengroup.org/onlinepubs/7908799/xbd/utilconv.html"
 * @author lcram
 */
public class SimpleOptionAndArgumentParser
{
    /**
     * @author lcram
     */
    public class ArgumentException extends Exception
    {
        private static final long serialVersionUID = 8506034533362610699L;

        public ArgumentException(final String message)
        {
            super(message);
        }
    }

    /**
     * @author lcram
     */
    public class OptionParseException extends Exception
    {
        private static final long serialVersionUID = 2471393426772482019L;

        public OptionParseException(final String message)
        {
            super(message);
        }
    }

    /**
     * A simple option representation. Store the option long/short form as well as metadata about
     * the option.
     *
     * @author lcram
     */
    public class SimpleOption implements Comparable<SimpleOption>
    {
        private final String longForm;
        private final Optional<Character> shortForm;
        private final String description;

        // Default values for option argument fields
        private OptionArgumentType argumentType = OptionArgumentType.NONE;
        private Optional<String> argumentHint = Optional.empty();

        SimpleOption(final String longForm, final Character shortForm, final String description)
        {
            if (longForm == null || longForm.isEmpty())
            {
                throw new CoreException("Long option form cannot be null or empty");
            }
            if (shortForm != null)
            {
                if (!Character.isLetterOrDigit(shortForm))
                {
                    throw new CoreException("Invalid short option form {}: must be letter or digit",
                            shortForm);
                }
            }
            if (description == null || description.isEmpty())
            {
                throw new CoreException("Description cannot be null or empty");
            }

            this.longForm = longForm;
            this.shortForm = Optional.ofNullable(shortForm);
            this.description = description;
        }

        SimpleOption(final String longForm, final Character shortForm, final String description,
                final OptionArgumentType argumentType, final String argumentHint)
        {
            this(longForm, shortForm, description);
            this.argumentType = argumentType;
            if (argumentHint != null && !argumentHint.isEmpty())
            {
                final String[] split = argumentHint.split("\\s+");
                if (split.length > 1)
                {
                    throw new CoreException("Option argument hint cannot contain whitespace");
                }
                this.argumentHint = Optional.of(argumentHint);
            }
            else
            {
                throw new CoreException("Option argument hint cannot be null or empty");
            }
        }

        SimpleOption(final String longForm, final OptionArgumentType argumentType,
                final String description, final String argumentHint)
        {
            this(longForm, null, description, argumentType, argumentHint);
        }

        SimpleOption(final String longForm, final String description)
        {
            this(longForm, null, description);
        }

        @Override
        public int compareTo(final SimpleOption other)
        {
            final String otherCaps = other.longForm.toUpperCase();
            final String thisCaps = this.longForm.toUpperCase();
            return thisCaps.compareTo(otherCaps);
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof SimpleOption)
            {
                if (this == other)
                {
                    return true;
                }
                final SimpleOption that = (SimpleOption) other;
                if (!Objects.equals(this.longForm, that.longForm))
                {
                    return false;
                }
                return true;
            }
            return false;
        }

        public Optional<String> getArgumentHint()
        {
            return this.argumentHint;
        }

        public OptionArgumentType getArgumentType()
        {
            return this.argumentType;
        }

        public String getDescription()
        {
            return this.description;
        }

        public String getLongForm()
        {
            return this.longForm;
        }

        public Optional<Character> getShortForm()
        {
            return this.shortForm;
        }

        @Override
        public int hashCode()
        {
            final int initialPrime = 31;
            final int hashSeed = 37;

            final int hash = hashSeed * initialPrime + Objects.hashCode(this.longForm);

            return hash;
        }

        @Override
        public String toString()
        {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.longForm);
            if (this.shortForm.isPresent())
            {
                builder.append(", " + this.shortForm.get());
            }
            return builder.toString();
        }
    }

    /**
     * @author lcram
     */
    public class UnknownOptionException extends Exception
    {
        private static final long serialVersionUID = 8506034533362610699L;

        private final String optionName;

        public UnknownOptionException(final String message)
        {
            super(message);
            this.optionName = message;
        }

        public String getOptionName()
        {
            return this.optionName;
        }
    }

    private static final Logger logger = LoggerFactory
            .getLogger(SimpleOptionAndArgumentParser.class);

    public static final String LONG_FORM_PREFIX = "--";
    public static final String SHORT_FORM_PREFIX = "-";
    public static final String OPTION_ARGUMENT_DELIMITER = "=";
    public static final String END_OPTIONS_OPERATOR = "--";

    private static final String DEFAULT_LONG_HELP = LONG_FORM_PREFIX + "help";
    private static final String DEFAULT_SHORT_HELP = SHORT_FORM_PREFIX + "h";
    private static final String DEFAULT_LONG_VERSION = LONG_FORM_PREFIX + "version";
    private static final String DEFAULT_SHORT_VERSION = SHORT_FORM_PREFIX + "V";

    private final Set<SimpleOption> registeredOptions;
    private final Map<String, ArgumentArity> registeredArgumentHintToArity;
    private final Map<String, ArgumentOptionality> registeredArgumentHintToOptionality;

    private final Set<String> longFormsSeen;
    private final Set<Character> shortFormsSeen;
    private final Set<String> argumentHintsSeen;

    private boolean registeredVariadicArgument;
    private boolean registeredOptionalArgument;

    private final Map<SimpleOption, Optional<String>> parsedOptions;
    private final Map<String, List<String>> parsedArguments;
    private boolean parseStepRan;

    public SimpleOptionAndArgumentParser()
    {
        this.registeredOptions = new LinkedHashSet<>();
        this.registeredArgumentHintToArity = new LinkedHashMap<>();
        this.registeredArgumentHintToOptionality = new LinkedHashMap<>();

        this.longFormsSeen = new HashSet<>();
        this.shortFormsSeen = new HashSet<>();
        this.argumentHintsSeen = new HashSet<>();

        this.registeredVariadicArgument = false;
        this.registeredOptionalArgument = false;

        this.parsedOptions = new LinkedHashMap<>();
        this.parsedArguments = new LinkedHashMap<>();
        this.parseStepRan = false;
    }

    /**
     * Get the mapping of registered argument hints to their arities.
     *
     * @return the mapping
     */
    public Map<String, ArgumentArity> getArgumentHintToArity()
    {
        return this.registeredArgumentHintToArity;
    }

    /**
     * Get the mapping of registered argument hints to their optionalities
     *
     * @return the mapping
     */
    public Map<String, ArgumentOptionality> getArgumentHintToOptionality()
    {
        return this.registeredArgumentHintToOptionality;
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
    public Optional<String> getOptionArgument(final String longForm)
    {
        if (!this.parseStepRan)
        {
            throw new CoreException("Cannot get options before parsing!");
        }
        if (!registeredOptionForLongForm(longForm).isPresent())
        {
            throw new CoreException("{} not a registered option", longForm);
        }
        final Optional<SimpleOption> option = getParsedOptionFromLongForm(longForm);
        if (option.isPresent())
        {
            return this.parsedOptions.get(option.get());
        }
        return Optional.empty();
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
    public <T> Optional<T> getOptionArgument(final String longForm,
            final StringConverter<T> converter)
    {
        if (!this.parseStepRan)
        {
            throw new CoreException("Cannot get options before parsing!");
        }
        if (!registeredOptionForLongForm(longForm).isPresent())
        {
            throw new CoreException("{} not a registered option", longForm);
        }
        final Optional<SimpleOption> option = getParsedOptionFromLongForm(longForm);
        if (option.isPresent())
        {
            final Optional<String> argument = this.parsedOptions.get(option.get());
            if (argument.isPresent())
            {
                final String argumentValue = argument.get();
                return Optional.ofNullable(converter.convert(argumentValue));
            }
        }
        return Optional.empty();
    }

    /**
     * Get the set of registered {@link SimpleOption}s.
     *
     * @return the set
     */
    public Set<SimpleOption> getRegisteredOptions()
    {
        return this.registeredOptions;
    }

    /**
     * Given a hint registered as a unary argument, return an optional wrapping the argument value
     * associated with that hint.
     *
     * @param hint
     *            the hint to check
     * @return an optional wrapping the value
     * @throws CoreException
     *             if the argument hint was not registered or is not unary
     */
    public Optional<String> getUnaryArgument(final String hint)
    {
        if (!this.parseStepRan)
        {
            throw new CoreException("Cannot get arguments before parsing!");
        }
        if (!this.registeredArgumentHintToArity.containsKey(hint))
        {
            throw new CoreException("hint \'{}\' does not correspond to a registered argument",
                    hint);
        }
        if (this.registeredArgumentHintToArity.get(hint) != ArgumentArity.UNARY)
        {
            throw new CoreException("hint \'{}\' does not correspond to a unary argument", hint);
        }
        final List<String> arguments = this.parsedArguments.get(hint);
        if (arguments != null && arguments.size() == 1)
        {
            return Optional.of(arguments.get(0));
        }

        logger.debug("No value found for unary argument {}, returning empty Optional", hint);
        return Optional.empty();
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
    public List<String> getVariadicArgument(final String hint)
    {
        if (!this.parseStepRan)
        {
            throw new CoreException("Cannot get arguments before parsing!");
        }
        if (!this.registeredArgumentHintToArity.containsKey(hint))
        {
            throw new CoreException("hint \'{}\' does not correspond to a registered argument",
                    hint);
        }
        if (this.registeredArgumentHintToArity.get(hint) != ArgumentArity.VARIADIC)
        {
            throw new CoreException("hint \'{}\' does not correspond to a variadic argument", hint);
        }
        final List<String> arguments = this.parsedArguments.get(hint);
        if (arguments != null)
        {
            return arguments;
        }

        logger.debug("No value found for variadic argument {}, returning empty List", hint);
        return new ArrayList<>();
    }

    /**
     * Check if a given long form option was supplied. This will return true even if only the short
     * form was actually present on the command line.
     *
     * @param longForm
     *            the long form option
     * @return if the option was supplied
     * @throws CoreException
     *             if longForm does not refer to a registered option
     */
    public boolean hasOption(final String longForm)
    {
        if (!this.parseStepRan)
        {
            throw new CoreException("Cannot get options before parsing!");
        }
        if (!registeredOptionForLongForm(longForm).isPresent())
        {
            throw new CoreException("{} not a registered option", longForm);
        }
        final Optional<SimpleOption> option = getParsedOptionFromLongForm(longForm);
        if (option.isPresent())
        {
            return true;
        }
        return false;
    }

    /**
     * Perform a full scan and parse of the provided arguments list. This method will populate the
     * parser's internal data structures so they are ready to be queried for results.
     *
     * @param allArguments
     *            The provided arguments list
     * @throws UnknownOptionException
     *             If an unknown option is detected
     * @throws OptionParseException
     *             If another parsing error occurs
     * @throws ArgumentException
     *             If supplied arguments do not match the registered argument hints
     */
    public void parseOptionsAndArguments(final List<String> allArguments)
            throws UnknownOptionException, OptionParseException, ArgumentException
    {
        final List<String> regularArguments = new ArrayList<>();
        boolean seenEndOptionsOperator = false;
        this.parsedArguments.clear();
        this.parsedOptions.clear();
        int regularArgumentCounter = 0;

        boolean skipNextArgument = false;

        for (int index = 0; index < allArguments.size(); index++)
        {
            if (skipNextArgument)
            {
                skipNextArgument = false;
                continue;
            }
            skipNextArgument = false;

            final String argument = allArguments.get(index);

            // We store a lookahead to use in case of an option with the argument specified like
            // "--option optarg". In this case we will need the lookahead value.
            Optional<String> lookahead = Optional.empty();
            if (index + 1 < allArguments.size())
            {
                lookahead = Optional.ofNullable(allArguments.get(index + 1));
            }

            // Five cases:
            // Argument is "--" -> stop parsing arguments as options
            // Argument is "-" -> treat as a regular argument
            // Argument starts with "--" -> long form option
            // Argument starts with "-" -> short form option
            // Anything else -> regular argument
            if (END_OPTIONS_OPERATOR.equals(argument))
            {
                if (seenEndOptionsOperator)
                {
                    regularArguments.add(argument);
                }
                else
                {
                    seenEndOptionsOperator = true;
                }
            }
            else if (SHORT_FORM_PREFIX.equals(argument))
            {
                regularArguments.add(argument);
            }
            else if (argument.startsWith(LONG_FORM_PREFIX) && !seenEndOptionsOperator)
            {
                final boolean consumedLookahead = parseLongFormOption(argument, lookahead);
                if (consumedLookahead)
                {
                    skipNextArgument = true;
                }
            }
            else if (argument.startsWith(SHORT_FORM_PREFIX) && !seenEndOptionsOperator)
            {
                final boolean consumedLookahead = parseShortFormOption(argument, lookahead);
                if (consumedLookahead)
                {
                    skipNextArgument = true;
                }
            }
            else
            {
                regularArguments.add(argument);
            }
        }

        if (this.registeredOptionalArgument)
        {
            if (regularArguments.size() < this.registeredArgumentHintToArity.size() - 1)
            {
                throw new ArgumentException("missing required argument(s)");
            }
        }
        else
        {
            if (regularArguments.size() < this.registeredArgumentHintToArity.size())
            {
                throw new ArgumentException("missing required argument(s)");
            }

        }

        // Now handle the regular arguments
        for (final String regularArgument : regularArguments)
        {
            regularArgumentCounter = parseRegularArgument(regularArgument, regularArguments.size(),
                    regularArgumentCounter);
        }

        this.parseStepRan = true;
    }

    /**
     * Register an argument with a given arity. The argument hint is used as a key to retrieve the
     * argument value(s) later. Additionally, documentation generators can use the hint to create
     * more accurate doc pages.
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
    public void registerArgument(final String argumentHint, final ArgumentArity arity,
            final ArgumentOptionality type)
    {
        throwIfArgumentHintSeen(argumentHint);

        if (argumentHint == null || argumentHint.isEmpty())
        {
            throw new CoreException("Argument hint cannot be null or empty");
        }

        final String[] split = argumentHint.split("\\s+");
        if (split.length > 1)
        {
            throw new CoreException("Option argument hint cannot contain whitespace");
        }

        if (this.registeredOptionalArgument)
        {
            throw new CoreException("Optional argument must be the last registered argument");
        }

        if (arity == ArgumentArity.VARIADIC)
        {
            if (this.registeredVariadicArgument)
            {
                throw new CoreException("Cannot register more than one variadic argument");
            }
        }
        if (type == ArgumentOptionality.OPTIONAL)
        {
            if (this.registeredOptionalArgument)
            {
                throw new CoreException("Cannot register more than one optional argument");
            }
            if (this.registeredVariadicArgument)
            {
                throw new CoreException(
                        "Cannot register both an optional argument and a variadic argument");
            }
            this.registeredOptionalArgument = true;
        }

        if (arity == ArgumentArity.VARIADIC)
        {
            this.registeredVariadicArgument = true;
        }

        this.registeredArgumentHintToArity.put(argumentHint, arity);
        this.registeredArgumentHintToOptionality.put(argumentHint, type);
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
    public void registerOption(final String longForm, final Character shortForm,
            final String description)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        if (shortForm != null)
        {
            throwIfDuplicateShortForm(shortForm);
            this.shortFormsSeen.add(shortForm);
        }
        this.registeredOptions.add(new SimpleOption(longForm, shortForm, description));
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
    public void registerOption(final String longForm, final String description)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        this.registeredOptions.add(new SimpleOption(longForm, description));
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
    public void registerOptionWithOptionalArgument(final String longForm, final Character shortForm,
            final String description, final String argumentHint)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        if (shortForm != null)
        {
            throwIfDuplicateShortForm(shortForm);
            this.shortFormsSeen.add(shortForm);
        }
        this.registeredOptions.add(new SimpleOption(longForm, shortForm, description,
                OptionArgumentType.OPTIONAL, argumentHint));
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
    public void registerOptionWithOptionalArgument(final String longForm, final String description,
            final String argumentHint)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        this.registeredOptions.add(
                new SimpleOption(longForm, OptionArgumentType.OPTIONAL, description, argumentHint));
    }

    /**
     * Register an option with a given long and short form that takes a required argument. The
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
    public void registerOptionWithRequiredArgument(final String longForm, final Character shortForm,
            final String description, final String argumentHint)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        if (shortForm != null)
        {
            throwIfDuplicateShortForm(shortForm);
            this.shortFormsSeen.add(shortForm);
        }
        this.registeredOptions.add(new SimpleOption(longForm, shortForm, description,
                OptionArgumentType.REQUIRED, argumentHint));
    }

    /**
     * Register an option with a given long form that takes a required argument. The provided
     * argument hint can be used for generated documentation, and should be a single word describing
     * the argument. The parser will throw an exception at parse-time if the argument is not
     * supplied.
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
    public void registerOptionWithRequiredArgument(final String longForm, final String description,
            final String argumentHint)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        this.registeredOptions.add(
                new SimpleOption(longForm, OptionArgumentType.REQUIRED, description, argumentHint));
    }

    /**
     * Perform a quick scan for any argument matching "--help" or "-h". This will not invoke any of
     * the underlying parsing machinery, and is useful for determining if a user was trying to get
     * help.
     *
     * @param allArguments
     *            The provided arguments list.
     * @return True if the user tried to provide a help flag, false otherwise.
     */
    public boolean scanForHelpFlag(final List<String> allArguments)
    {
        for (final String argument : allArguments)
        {
            if (DEFAULT_SHORT_HELP.equals(argument) || DEFAULT_LONG_HELP.equals(argument))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform a quick scan for any argument matching "--version" or "-V". This will not invoke any
     * of the underlying parsing machinery, and is useful for determining if a user was trying to
     * get the version.
     *
     * @param allArguments
     *            The provided arguments list.
     * @return True if the user tried to provide a version flag, false otherwise.
     */
    public boolean scanForVersionFlag(final List<String> allArguments)
    {
        for (final String argument : allArguments)
        {
            if (DEFAULT_SHORT_VERSION.equals(argument) || DEFAULT_LONG_VERSION.equals(argument))
            {
                return true;
            }
        }
        return false;
    }

    private Optional<SimpleOption> checkForLongOption(final String longForm,
            final Set<SimpleOption> setToCheck)
    {
        for (final SimpleOption option : setToCheck)
        {
            if (option.getLongForm().equals(longForm))
            {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    private Optional<SimpleOption> checkForShortOption(final Character shortForm,
            final Set<SimpleOption> setToCheck)
    {
        for (final SimpleOption option : setToCheck)
        {
            final Optional<Character> optionalForm = option.getShortForm();
            if (optionalForm.isPresent())
            {
                if (optionalForm.get().equals(shortForm))
                {
                    return Optional.of(option);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<SimpleOption> getParsedOptionFromLongForm(final String longForm)
    {
        return checkForLongOption(longForm, this.parsedOptions.keySet());
    }

    /*
     * This function returns a boolean value specifying whether or not it consumed the lookahead
     * value.
     */
    private boolean parseLongFormOption(final String argument, final Optional<String> lookahead)
            throws UnknownOptionException, OptionParseException
    {
        final String scrubbedPrefix = argument.substring(LONG_FORM_PREFIX.length());
        final String[] split = scrubbedPrefix.split(OPTION_ARGUMENT_DELIMITER, 2);
        final String optionName = split[0];

        final Optional<SimpleOption> option = registeredOptionForLongForm(optionName);

        if (option.isPresent())
        {
            // Split length is 1 if command line looks like "... --option anotherThing ..."
            // Split length is > 1 if command line looks like "... --option=arg anotherThing ..."
            // Split length will never be < 1
            if (split.length == 1)
            {
                // Cases to handle here regarding the lookahead
                // 1) The option takes no argument or an optional argument -> do not use lookahead
                // 2) The option takes a required argument -> attempt to use lookahead
                // Once done, we return whether or not we used the lookahead
                switch (option.get().getArgumentType())
                {
                    case NONE:
                        // fallthru intended
                    case OPTIONAL:
                        this.parsedOptions.put(option.get(), Optional.empty());
                        return false;
                    case REQUIRED:
                        if (lookahead.isPresent())
                        {
                            this.parsedOptions.put(option.get(), lookahead);
                            return true;
                        }
                        else
                        {
                            throw new OptionParseException("option \'" + option.get().getLongForm()
                                    + "\' needs an argument");
                        }
                    default:
                        throw new CoreException("Unrecognized OptionArgumentType {}",
                                option.get().getArgumentType());
                }
            }
            else
            {
                // Cases to handle here
                // 1) The option takes no argument -> throw an error
                // 2) The option takes an optional or required argument -> use the split
                final String optionArgument = split[1];
                switch (option.get().getArgumentType())
                {
                    case NONE:
                        throw new OptionParseException(
                                "option \'" + option.get().getLongForm() + "\' takes no argument");
                    case OPTIONAL:
                        // fallthru intended
                    case REQUIRED:
                        this.parsedOptions.put(option.get(), Optional.ofNullable(optionArgument));
                        return false;
                    default:
                        throw new CoreException("Unrecognized OptionArgumentType {}",
                                option.get().getArgumentType());
                }
            }
        }
        else
        {
            throw new UnknownOptionException(optionName);
        }
    }

    private int parseRegularArgument(final String argument, final int regularArgumentSize,
            final int regularArgumentCounter) throws ArgumentException
    {
        int argumentCounter = regularArgumentCounter;
        if (argumentCounter >= this.registeredArgumentHintToArity.size())
        {
            throw new ArgumentException("too many arguments");
        }

        final String argumentHint = (String) this.registeredArgumentHintToArity.keySet()
                .toArray()[argumentCounter];
        final ArgumentArity currentArity = this.registeredArgumentHintToArity.get(argumentHint);
        switch (currentArity)
        {
            case UNARY:
                logger.debug("parsed unary argument hint => {} : value => {}", argumentHint,
                        argument);
                this.parsedArguments.put(argumentHint, Arrays.asList(argument));
                argumentCounter++;
                break;
            case VARIADIC:
                List<String> multiArgumentList = this.parsedArguments.get(argumentHint);
                multiArgumentList = multiArgumentList == null ? new ArrayList<>()
                        : multiArgumentList;
                multiArgumentList.add(argument);
                logger.debug("parsed variadic argument hint => {} : value => {}", argumentHint,
                        argument);
                this.parsedArguments.put(argumentHint, multiArgumentList);

                // Two cases:
                // Case 1 -> [UNARY...] VARIADIC
                if (argumentCounter == this.registeredArgumentHintToArity.size() - 1)
                {
                    // do nothing, we can consume the rest of the arguments
                }
                // Case 2 -> [UNARY...] VARIADIC UNARY [UNARY...]
                else
                {
                    // cutoff point, be sure to save arguments for consumption by subsequent hints
                    if (multiArgumentList.size() == regularArgumentSize
                            - this.registeredArgumentHintToArity.size() + 1)
                    {
                        argumentCounter++;
                        break;
                    }
                }
                break;
            default:
                throw new CoreException("Unrecognized ArgumentArity {}", currentArity);
        }
        return argumentCounter;
    }

    /*
     * This function returns a boolean value specifying whether or not it consumed the lookahead
     * value.
     */
    private boolean parseShortFormOption(final String argument, final Optional<String> lookahead)
            throws OptionParseException, UnknownOptionException
    {
        final String scrubbedPrefix = argument.substring(SHORT_FORM_PREFIX.length());

        // Two cases
        // 1) command line looks like "... -o arg ..."
        // 2) command line looks like "... -oarg ..."
        // scrubbedPrefix length will never be < 1

        // Case 1) "... -o arg ..."
        if (scrubbedPrefix.length() == 1)
        {
            final Optional<SimpleOption> option = registeredOptionForShortForm(
                    scrubbedPrefix.charAt(0));

            if (!option.isPresent())
            {
                throw new UnknownOptionException(String.valueOf(scrubbedPrefix));
            }

            // 3 cases to handle here regarding the option argument type
            // a) The option takes no argument -> do not use lookahead
            // b) The option takes an optional argument -> do not use lookahead
            // c) The option takes a required argument -> attempt to use lookahead
            // Once done, we return whether or not we used the lookahead
            switch (option.get().getArgumentType())
            {
                case NONE:
                    // fallthru intended
                case OPTIONAL:
                    this.parsedOptions.put(option.get(), Optional.empty());
                    return false;
                case REQUIRED:
                    if (lookahead.isPresent())
                    {
                        this.parsedOptions.put(option.get(), lookahead);
                        return true;
                    }
                    else
                    {
                        throw new OptionParseException("option \'"
                                + option.get().getShortForm().get() + "\' needs an argument");
                    }
                default:
                    throw new CoreException("Bad OptionArgumentType {}",
                            option.get().getArgumentType());
            }
        }
        // Case 2) "... -oarg ..."
        else
        {
            // Cases to handle here
            // a) The option is using bundling, ie. ("-oarg" meaning "-o -a -r -g")
            // b) The option is using an argument, ie. ("-oarg" where "arg" is an argument to "-o")

            // Check for case a) determine if valid bundle
            boolean isValidBundle = true;
            for (int index = 0; index < scrubbedPrefix.length(); index++)
            {
                final char optionCharacter = scrubbedPrefix.charAt(index);
                final Optional<SimpleOption> option = registeredOptionForShortForm(optionCharacter);
                if (option.isPresent())
                {
                    if (option.get().getArgumentType() != OptionArgumentType.NONE)
                    {
                        isValidBundle = false;
                        break;
                    }
                }
                else
                {
                    isValidBundle = false;
                    break;
                }
            }

            if (isValidBundle)
            {
                // Bundle was valid, so loop over again and add all options
                for (int index = 0; index < scrubbedPrefix.length(); index++)
                {
                    final char optionCharacter = scrubbedPrefix.charAt(index);
                    final Optional<SimpleOption> option = registeredOptionForShortForm(
                            optionCharacter);
                    this.parsedOptions.put(option.get(), Optional.empty());
                }
            }
            else
            {
                // Bundle was not valid, so treat remaining chars as an option arg
                final char optionCharacter = scrubbedPrefix.charAt(0);
                final Optional<SimpleOption> option = registeredOptionForShortForm(optionCharacter);
                if (!option.isPresent())
                {
                    throw new UnknownOptionException(String.valueOf(optionCharacter));
                }
                if (option.get().getArgumentType() == OptionArgumentType.NONE)
                {
                    throw new OptionParseException("option \'" + option.get().getShortForm().get()
                            + "\' takes no argument");
                }
                final String optionArgument = scrubbedPrefix.substring(1);
                this.parsedOptions.put(option.get(), Optional.ofNullable(optionArgument));
            }

            return false;
        }
    }

    private Optional<SimpleOption> registeredOptionForLongForm(final String longForm)
    {
        return checkForLongOption(longForm, this.registeredOptions);
    }

    private Optional<SimpleOption> registeredOptionForShortForm(final Character shortForm)
    {
        return checkForShortOption(shortForm, this.registeredOptions);
    }

    private void throwIfArgumentHintSeen(final String hint)
    {
        if (this.argumentHintsSeen.contains(hint))
        {
            throw new CoreException("Cannot register argument hint {} more than once!", hint);
        }
    }

    private void throwIfDuplicateLongForm(final String longForm)
    {
        if (this.longFormsSeen.contains(longForm))
        {
            throw new CoreException("Cannot register option {} more than once!", longForm);
        }
    }

    private void throwIfDuplicateShortForm(final Character shortForm)
    {
        if (this.shortFormsSeen.contains(shortForm))
        {
            throw new CoreException("Cannot register option {} more than once!", shortForm);
        }
    }
}

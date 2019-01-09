package org.openstreetmap.atlas.utilities.command.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.AmbiguousAbbreviationException;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.ArgumentException;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.OptionParseException;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.UnknownOptionException;
import org.openstreetmap.atlas.utilities.command.parsing.exceptions.UnparsableContextException;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple option and argument parser, designed specifically to impose constraints on the format of
 * the arguments and options. Non-ambiguity is enforced at registration time. Once you have
 * successfully registered the parser, you can be sure it will parse any input command line as
 * expected, throwing errors where appropriate. Nothing about this class is thread safe, should you
 * decide to parse in one thread and read results in another.
 * <p>
 * Supports multiple types of arguments:<br>
 * OPTIONAL vs REQUIRED: if an argument marked REQUIRED is not supplied, the parser will throw an
 * error<br>
 * UNARY vs VARIADIC: a VARIADIC argument is one that can consist of an arbitrary number of
 * values<br>
 * <br>
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
 * version in the highest ARGV position (ie. the furthest right on the command line).
 * </p>
 * This class supports both the POSIX short option spec as well as the GNU long option spec. See
 * included links for details.<br>
 * <br>
 * This class supports long option prefix abbreviations. This means that a long option "--option"
 * can be abbreviated on the command line as "--o" or "--op" or any non-ambiguous prefix. If an
 * abbreviation results in ambiguity, the parser will throw an error at parse-time.<br>
 * <br>
 * Note that this class also supports multiple parsing contexts, if desired. A parsing context
 * corresponds to certain usage case. For example, you can register a context with ID 3 that takes a
 * single argument and the option "--opt1". Then you can also define a context ID 4 that takes 2
 * arguments and an option "--opt2". The parser will automatically figure out which context is
 * implied from the supplied command line. If more than one context matches, the context with the
 * lowest numerical ID is selected. If no matching contexts can be found, the parser throws an error
 * with a diagnostic message explaining what happened.<br>
 * <br>
 *
 * @see "https://www.gnu.org/software/libc/manual/html_node/Argument-Syntax.html"
 * @see "http://pubs.opengroup.org/onlinepubs/9699919799/basedefs/V1_chap12.html"
 * @see "http://pubs.opengroup.org/onlinepubs/7908799/xbd/utilconv.html"
 * @author lcram
 */
public class SimpleOptionAndArgumentParser
{
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
        private final OptionOptionality optionality;

        // Default values for option argument fields
        private OptionArgumentType argumentType = OptionArgumentType.NONE;
        private Optional<String> argumentHint = Optional.empty();

        SimpleOption(final String longForm, final Character shortForm, final String description,
                final OptionOptionality optionality, final OptionArgumentType argumentType,
                final String argumentHint)
        {
            if (longForm == null || longForm.isEmpty())
            {
                throw new CoreException("Long option form cannot be null or empty");
            }
            if (shortForm != null && !Character.isLetterOrDigit(shortForm))
            {
                throw new CoreException("Invalid short option form {}: must be letter or digit",
                        shortForm);
            }
            if (description == null || description.isEmpty())
            {
                throw new CoreException("Description cannot be null or empty");
            }
            this.longForm = longForm;
            this.shortForm = Optional.ofNullable(shortForm);
            this.description = description;
            this.optionality = optionality;

            this.argumentType = argumentType;
            if (this.argumentType != OptionArgumentType.NONE)
            {
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
                return Objects.equals(this.longForm, that.longForm);
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

        public OptionOptionality getOptionality()
        {
            return this.optionality;
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

            return hashSeed * initialPrime + Objects.hashCode(this.longForm);
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

    private static final String PROVIDED_OPTION_LONG_FORM_WAS_AMBIGUOUS = "provided option long form {} was ambiguous";
    private static final String CANNOT_GET_OPTIONS_BEFORE_PARSING = "Cannot get options before parsing!";

    private static final Logger logger = LoggerFactory
            .getLogger(SimpleOptionAndArgumentParser.class);

    public static final String LONG_FORM_PREFIX = "--";
    public static final String SHORT_FORM_PREFIX = "-";
    public static final String OPTION_ARGUMENT_DELIMITER = "=";
    public static final String END_OPTIONS_OPERATOR = "--";

    public static final int NO_CONTEXT = 0;

    private final Map<Integer, Set<SimpleOption>> contextToRegisteredOptions;
    private final Map<Integer, Map<String, ArgumentArity>> contextToArgumentHintToArity;
    private final Map<Integer, Map<String, ArgumentOptionality>> contextToArgumentHintToOptionality;
    private final Map<Integer, Boolean> contextToRegisteredVariadicArgument;
    private final Map<Integer, Boolean> contextToRegisteredOptionalArgument;
    private final SortedSet<Integer> registeredContexts;

    private final Set<String> longFormsSeen;
    private final Set<Character> shortFormsSeen;
    private final Set<String> argumentHintsSeen;

    private final Map<SimpleOption, Optional<String>> parsedOptions;
    private final Map<String, List<String>> parsedArguments;
    private int currentContext;
    private boolean parseStepRanAtLeastOnce;

    public SimpleOptionAndArgumentParser()
    {
        this.contextToRegisteredOptions = new HashMap<>();
        this.contextToArgumentHintToArity = new HashMap<>();
        this.contextToArgumentHintToOptionality = new HashMap<>();
        this.contextToRegisteredVariadicArgument = new HashMap<>();
        this.contextToRegisteredOptionalArgument = new HashMap<>();
        this.registeredContexts = new TreeSet<>();

        this.longFormsSeen = new HashSet<>();
        this.shortFormsSeen = new HashSet<>();
        this.argumentHintsSeen = new HashSet<>();

        this.parsedOptions = new LinkedHashMap<>();
        this.parsedArguments = new LinkedHashMap<>();
        this.currentContext = NO_CONTEXT;
        this.parseStepRanAtLeastOnce = false;
    }

    /**
     * Get the mapping of registered argument hints to their arities.
     *
     * @return the mapping
     */
    public Map<Integer, Map<String, ArgumentArity>> getArgumentHintToArity()
    {
        return this.contextToArgumentHintToArity;
    }

    /**
     * Get the mapping of registered argument hints to their optionalities
     *
     * @return the mapping
     */
    public Map<Integer, Map<String, ArgumentOptionality>> getArgumentHintToOptionality()
    {
        return this.contextToArgumentHintToOptionality;
    }

    public int getContext()
    {
        return this.currentContext;
    }

    public Map<Integer, Set<SimpleOption>> getContextToRegisteredOptions()
    {
        return this.contextToRegisteredOptions;
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
        if (!this.parseStepRanAtLeastOnce)
        {
            throw new CoreException(CANNOT_GET_OPTIONS_BEFORE_PARSING);
        }
        final Optional<SimpleOption> option;
        try
        {
            if (!registeredOptionForLongForm(this.currentContext, longForm).isPresent())
            {
                throw new CoreException("{} not a registered option", longForm);
            }
            option = getParsedOptionFromLongForm(longForm);
        }
        catch (final AmbiguousAbbreviationException exception)
        {
            throw new CoreException(PROVIDED_OPTION_LONG_FORM_WAS_AMBIGUOUS, longForm);
        }
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
        if (!this.parseStepRanAtLeastOnce)
        {
            throw new CoreException(CANNOT_GET_OPTIONS_BEFORE_PARSING);
        }
        final Optional<SimpleOption> option;
        try
        {
            if (!registeredOptionForLongForm(this.currentContext, longForm).isPresent())
            {
                throw new CoreException("{} not a registered option", longForm);
            }
            option = getParsedOptionFromLongForm(longForm);
        }
        catch (final AmbiguousAbbreviationException exception)
        {
            throw new CoreException(PROVIDED_OPTION_LONG_FORM_WAS_AMBIGUOUS, longForm);
        }
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
     * Get the registered contexts for this parser.
     *
     * @return the set
     */
    public SortedSet<Integer> getRegisteredContexts()
    {
        return this.registeredContexts;
    }

    /**
     * Get the set of registered {@link SimpleOption}s.
     *
     * @return the set
     */
    public Set<SimpleOption> getRegisteredOptions()
    {
        final Set<SimpleOption> allOptions = new HashSet<>();
        for (final Integer context : this.registeredContexts)
        {
            allOptions.addAll(this.contextToRegisteredOptions.get(context));
        }
        return allOptions;
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
        if (!this.parseStepRanAtLeastOnce)
        {
            throw new CoreException("Cannot get arguments before parsing!");
        }
        if (!this.contextToArgumentHintToArity.get(this.currentContext).containsKey(hint))
        {
            return Optional.empty();
        }
        if (this.contextToArgumentHintToArity.get(this.currentContext)
                .get(hint) != ArgumentArity.UNARY)
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
        if (!this.parseStepRanAtLeastOnce)
        {
            throw new CoreException("Cannot get arguments before parsing!");
        }
        if (!this.contextToArgumentHintToArity.containsKey(this.currentContext)
                || !this.contextToArgumentHintToArity.get(this.currentContext).containsKey(hint))
        {
            throw new CoreException(
                    "hint \'{}\' does not correspond to a registered argument in context {}", hint,
                    this.currentContext);
        }
        if (this.contextToArgumentHintToArity.get(this.currentContext)
                .get(hint) != ArgumentArity.VARIADIC)
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
        if (!this.parseStepRanAtLeastOnce)
        {
            throw new CoreException(CANNOT_GET_OPTIONS_BEFORE_PARSING);
        }
        final Optional<SimpleOption> option;
        try
        {
            if (!registeredOptionForLongForm(this.currentContext, longForm).isPresent())
            {
                return false;
            }
            option = getParsedOptionFromLongForm(longForm);
        }
        catch (final AmbiguousAbbreviationException exception)
        {
            throw new CoreException(PROVIDED_OPTION_LONG_FORM_WAS_AMBIGUOUS, longForm);
        }
        return option.isPresent();
    }

    public boolean isEmpty()
    {
        return this.parsedOptions.isEmpty() && this.parsedArguments.isEmpty();
    }

    public void parse(final List<String> allArguments) throws AmbiguousAbbreviationException, // NOSONAR
            UnknownOptionException, UnparsableContextException
    {
        this.parsedArguments.clear();
        this.parsedOptions.clear();
        this.currentContext = NO_CONTEXT;
        boolean seenEndOptionsOperator = false;

        /*
         * First, we pre-parse arguments to see if there are any ambiguous or unknown long options.
         * This will help generate better error message for the end user. This check must happen
         * independent of any parsing context, since you need to be able to disambiguate option
         * prefix abbreviations before a context is selected. Consider the following example:
         */
        // Parser Context ID 3 has option --opt1
        // Parser Context ID 4 has option --opt2
        // User supplies option --opt
        /*
         * In this case we want to throw an error early, warning that the option is ambiguous. If we
         * didn't, the parser context selection code would choose context 3 (since it picks the
         * first context that does not throw a parse error). This is not intuitive behaviour for end
         * users, who need not know about the mechanics of parser contexts.
         */
        for (final String argument : allArguments)
        {
            if (END_OPTIONS_OPERATOR.equals(argument))
            {
                if (!seenEndOptionsOperator)
                {
                    seenEndOptionsOperator = true;
                }
            }
            else if (SHORT_FORM_PREFIX.equals(argument))
            {
                continue; // NOSONAR
            }
            else if (argument.startsWith(LONG_FORM_PREFIX) && !seenEndOptionsOperator)
            {
                final String[] split = argument.substring(LONG_FORM_PREFIX.length())
                        .split(OPTION_ARGUMENT_DELIMITER, 2);
                final String optionName = split[0];
                final Optional<SimpleOption> option = checkForLongOption(optionName,
                        getRegisteredOptions(), true);
                if (!option.isPresent())
                {
                    throw new UnknownOptionException(optionName, getRegisteredOptions());
                }
            }
            else if (argument.startsWith(SHORT_FORM_PREFIX) && !seenEndOptionsOperator)
            {
                final Optional<SimpleOption> option = checkForShortOption(argument.charAt(1),
                        getRegisteredOptions());
                if (!option.isPresent())
                {
                    throw new UnknownOptionException(argument.charAt(1));
                }
            }
        }

        final SortedSet<String> exceptionMessagesWeSaw = new TreeSet<>();
        // Now we actually parse the arguments, assigning a context.
        for (final Integer context : this.registeredContexts) // NOSONAR
        {
            try
            {
                this.parseOptionsAndArguments(allArguments, context);
            }
            catch (final Exception exception)
            {
                exceptionMessagesWeSaw.add(String.format("%d: %s (context %d)", context,
                        exception.getMessage(), context));
                continue;
            }

            this.currentContext = context;
            break;
        }

        if (this.currentContext == NO_CONTEXT)
        {
            throw new UnparsableContextException(exceptionMessagesWeSaw);
        }

        this.parseStepRanAtLeastOnce = true;
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
     * @param optionality
     *            whether the argument is optional or required
     * @param contexts
     *            the contexts for this argument, if not provided then uses a default context
     * @throws CoreException
     *             if the argument could not be registered
     */
    public void registerArgument(final String argumentHint, final ArgumentArity arity,
            final ArgumentOptionality optionality, final Integer... contexts)
    {
        throwIfArgumentHintSeen(argumentHint);
        this.argumentHintsSeen.add(argumentHint);

        if (argumentHint == null || argumentHint.isEmpty())
        {
            throw new CoreException("Argument hint cannot be null or empty");
        }

        final String[] split = argumentHint.split("\\s+");
        if (split.length > 1)
        {
            throw new CoreException("Option argument hint cannot contain whitespace");
        }

        if (contexts.length == 0)
        {
            throw new CoreException("Must provide at least one context.");
        }
        for (int i = 0; i < contexts.length; i++)
        {
            registerArgumentHelper(contexts[i], argumentHint, arity, optionality);
        }
    }

    /**
     * Register a given context with no options or arguments. If the context already exists, this
     * will noop.
     *
     * @param context
     *            the context to register
     */
    public void registerEmptyContext(final int context)
    {
        if (this.registeredContexts.contains(context))
        {
            logger.info("Tried to register empty context {}, but {} is already registered", context,
                    context);
            return;
        }
        this.registeredContexts.add(context);
        this.contextToRegisteredOptions.put(context, new HashSet<>());
        this.contextToRegisteredOptionalArgument.put(context, false);
        this.contextToArgumentHintToArity.put(context, new HashMap<>());
        this.contextToArgumentHintToOptionality.put(context, new HashMap<>());
        this.contextToRegisteredVariadicArgument.put(context, false);
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
     *            the contexts for this option, if not provided then uses a default context
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOption(final String longForm, final Character shortForm,
            final String description, final OptionOptionality optionality,
            final Integer... contexts)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        if (contexts.length == 0)
        {
            throw new CoreException("Must register at least one context.");
        }
        for (int i = 0; i < contexts.length; i++)
        {
            registerOptionHelper(contexts[i], longForm, shortForm, description, optionality,
                    OptionArgumentType.NONE, null);
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
     *            the contexts for this option, if not provided then uses a default context
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOption(final String longForm, final String description,
            final OptionOptionality optionality, final Integer... contexts)
    {
        this.registerOption(longForm, null, description, optionality, contexts);
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
     *            the contexts for this option, if not provided then uses a default context
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOptionWithOptionalArgument(final String longForm, final Character shortForm,
            final String description, final OptionOptionality optionality,
            final String argumentHint, final Integer... contexts)
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
        if (contexts.length == 0)
        {
            throw new CoreException("Must register at least one context.");
        }
        for (int i = 0; i < contexts.length; i++)
        {
            registerOptionHelper(contexts[i], longForm, shortForm, description, optionality,
                    OptionArgumentType.OPTIONAL, argumentHint);
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
     *            the contexts for this option, if not provided then uses a default context
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOptionWithOptionalArgument(final String longForm, final String description,
            final OptionOptionality optionality, final String argumentHint,
            final Integer... contexts)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        if (contexts.length == 0)
        {
            throw new CoreException("Must register at least one context.");
        }
        for (int i = 0; i < contexts.length; i++)
        {
            registerOptionHelper(contexts[i], longForm, null, description, optionality,
                    OptionArgumentType.OPTIONAL, argumentHint);
        }
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
     * @param optionality
     *            the optionality
     * @param argumentHint
     *            the hint for the argument
     * @param contexts
     *            the contexts for this option, if not provided then uses a default context
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOptionWithRequiredArgument(final String longForm, final Character shortForm,
            final String description, final OptionOptionality optionality,
            final String argumentHint, final Integer... contexts)
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
        if (contexts.length == 0)
        {
            throw new CoreException("Must register at least one context.");
        }
        for (int i = 0; i < contexts.length; i++)
        {
            registerOptionHelper(contexts[i], longForm, shortForm, description, optionality,
                    OptionArgumentType.REQUIRED, argumentHint);
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
     * @param description
     *            a simple description
     * @param optionality
     *            the optionality
     * @param argumentHint
     *            the hint for the argument
     * @param contexts
     *            the contexts for this option, if not provided then uses a default context
     * @throws CoreException
     *             if the option could not be registered
     */
    public void registerOptionWithRequiredArgument(final String longForm, final String description,
            final OptionOptionality optionality, final String argumentHint,
            final Integer... contexts)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        if (contexts.length == 0)
        {
            throw new CoreException("Must register at least one context.");
        }
        for (int i = 0; i < contexts.length; i++)
        {
            registerOptionHelper(contexts[i], longForm, null, description, optionality,
                    OptionArgumentType.REQUIRED, argumentHint);
        }
    }

    private Optional<SimpleOption> checkForLongOption(final String longForm,
            final Set<SimpleOption> setToCheck, final boolean usePrefixMatching)
            throws AmbiguousAbbreviationException
    {
        final Set<SimpleOption> matchedOptions = new HashSet<>();
        for (final SimpleOption option : setToCheck)
        {
            if (option.getLongForm().startsWith(longForm))
            {
                /*
                 * Break out if we find an exact match. This handles the edge case where you have
                 * two options like "--option" and "--optionSuffix". In this case, if "--option" is
                 * supplied, we want to return the exact match instead of throwing an ambiguity
                 * error.
                 */
                if (option.getLongForm().equals(longForm))
                {
                    return Optional.of(option);
                }
                if (usePrefixMatching)
                {
                    matchedOptions.add(option);
                }
            }
        }
        if (matchedOptions.size() > 1)
        {
            final List<String> ambiguousOptions = matchedOptions.stream()
                    .map(SimpleOption::getLongForm).collect(Collectors.toList());
            throw new AmbiguousAbbreviationException(longForm,
                    new StringList(ambiguousOptions).join(", "));
        }
        else if (matchedOptions.size() == 1)
        {
            final SimpleOption matchedOption = matchedOptions.toArray(new SimpleOption[0])[0];
            return Optional.of(matchedOption);
        }
        return Optional.empty();
    }

    private Optional<SimpleOption> checkForShortOption(final Character shortForm,
            final Set<SimpleOption> setToCheck)
    {
        for (final SimpleOption option : setToCheck)
        {
            final Optional<Character> optionalForm = option.getShortForm();
            if (optionalForm.isPresent() && optionalForm.get().equals(shortForm))
            {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    private Optional<SimpleOption> getParsedOptionFromLongForm(final String longForm)
            throws AmbiguousAbbreviationException
    {
        return checkForLongOption(longForm, this.parsedOptions.keySet(), false);
    }

    /*
     * This function returns a boolean value specifying whether or not it consumed the lookahead
     * value.
     */
    private boolean parseLongFormOption(final int tryContext, final String argument, // NOSONAR
            final Optional<String> lookahead)
            throws UnknownOptionException, OptionParseException, AmbiguousAbbreviationException
    {
        final String scrubbedPrefix = argument.substring(LONG_FORM_PREFIX.length());
        final String[] split = scrubbedPrefix.split(OPTION_ARGUMENT_DELIMITER, 2);
        final String optionName = split[0];

        final Optional<SimpleOption> option = registeredOptionForLongForm(tryContext, optionName);

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
                            throw new OptionParseException("option \'" + option.get().getLongForm() // NOSONAR
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

    /**
     * Perform a full scan and parse of the provided arguments list. This method will populate the
     * parser's internal data structures so they are ready to be queried for results. This method
     * tries to parse the arguments within a supplied context.
     *
     * @param allArguments
     *            The provided arguments list
     * @param tryContext
     *            the context to try
     * @throws UnknownOptionException
     *             If an unknown option is detected
     * @throws OptionParseException
     *             If another parsing error occurs
     * @throws ArgumentException
     *             If supplied arguments do not match the registered argument hints
     * @throws AmbiguousAbbreviationException
     *             If an ambiguous long option abbreviation was used
     */
    private void parseOptionsAndArguments(final List<String> allArguments, final int tryContext) // NOSONAR
            throws UnknownOptionException, OptionParseException, ArgumentException,
            AmbiguousAbbreviationException
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
                final boolean consumedLookahead = parseLongFormOption(tryContext, argument,
                        lookahead);
                if (consumedLookahead)
                {
                    skipNextArgument = true;
                }
            }
            else if (argument.startsWith(SHORT_FORM_PREFIX) && !seenEndOptionsOperator)
            {
                final boolean consumedLookahead = parseShortFormOption(tryContext, argument,
                        lookahead);
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

        // Check that any option registered as required is actually present. If not, throw an error.
        final Set<SimpleOption> registeredOptions = this.contextToRegisteredOptions.get(tryContext);
        if (registeredOptions != null)
        {
            for (final SimpleOption registeredOption : registeredOptions)
            {
                if (registeredOption.getOptionality() == OptionOptionality.REQUIRED
                        && !this.parsedOptions.keySet().contains(registeredOption))
                {
                    throw new OptionParseException(
                            "missing required option " + registeredOption.longForm);
                }
            }
        }

        if (this.contextToRegisteredOptionalArgument.getOrDefault(tryContext, false))
        {
            if (this.contextToArgumentHintToArity.containsKey(tryContext) && regularArguments
                    .size() < this.contextToArgumentHintToArity.get(tryContext).size() - 1)
            {
                throw new ArgumentException("missing required argument(s)");
            }
        }
        else
        {
            if (this.contextToArgumentHintToArity.containsKey(tryContext) && regularArguments
                    .size() < this.contextToArgumentHintToArity.get(tryContext).size())
            {
                throw new ArgumentException("missing required argument(s)");
            }
        }

        // Now handle the regular arguments
        for (final String regularArgument : regularArguments)
        {
            regularArgumentCounter = parseRegularArgument(tryContext, regularArgument,
                    regularArguments.size(), regularArgumentCounter);
        }

        this.parseStepRanAtLeastOnce = true;
    }

    private int parseRegularArgument(final int context, final String argument,
            final int regularArgumentSize, final int regularArgumentCounter)
            throws ArgumentException
    {
        int argumentCounter = regularArgumentCounter;

        if (!this.contextToArgumentHintToArity.containsKey(context))
        {
            throw new ArgumentException("too many arguments");
        }

        if (this.contextToArgumentHintToArity.containsKey(context)
                && argumentCounter >= this.contextToArgumentHintToArity.get(context).size())
        {
            throw new ArgumentException("too many arguments");
        }

        final String argumentHint = (String) this.contextToArgumentHintToArity.get(context).keySet()
                .toArray()[argumentCounter];
        final ArgumentArity currentArity = this.contextToArgumentHintToArity.get(context)
                .get(argumentHint);
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
                if (argumentCounter == this.contextToArgumentHintToArity.get(context).size() - 1)
                {
                    // do nothing, we can consume the rest of the arguments
                }
                // Case 2 -> [UNARY...] VARIADIC UNARY [UNARY...]
                else
                {
                    // cutoff point, be sure to save arguments for consumption by subsequent hints
                    if (multiArgumentList.size() == regularArgumentSize
                            - this.contextToArgumentHintToArity.get(context).size() + 1)
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
    private boolean parseShortFormOption(final int context, final String argument, // NOSONAR
            final Optional<String> lookahead) throws OptionParseException, UnknownOptionException
    {
        final String scrubbedPrefix = argument.substring(SHORT_FORM_PREFIX.length());

        // Two cases
        // 1) command line looks like "... -o arg ..."
        // 2) command line looks like "... -oarg ..."
        // scrubbedPrefix length will never be < 1

        // Case 1) "... -o arg ..."
        if (scrubbedPrefix.length() == 1)
        {
            final Optional<SimpleOption> option = registeredOptionForShortForm(context,
                    scrubbedPrefix.charAt(0));

            if (!option.isPresent())
            {
                throw new UnknownOptionException(scrubbedPrefix.charAt(0));
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
                                + option.get().getShortForm().get() + "\' needs an argument"); // NOSONAR
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
            for (int index = 0; index < scrubbedPrefix.length(); index++) // NOSONAR
            {
                final char optionCharacter = scrubbedPrefix.charAt(index);
                final Optional<SimpleOption> option = registeredOptionForShortForm(context,
                        optionCharacter);
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
                    final Optional<SimpleOption> option = registeredOptionForShortForm(context,
                            optionCharacter);
                    this.parsedOptions.put(option.get(), Optional.empty()); // NOSONAR
                }
            }
            else
            {
                // Bundle was not valid, so treat remaining chars as an option arg
                final char optionCharacter = scrubbedPrefix.charAt(0);
                final Optional<SimpleOption> option = registeredOptionForShortForm(context,
                        optionCharacter);
                if (!option.isPresent())
                {
                    throw new UnknownOptionException(String.valueOf(optionCharacter).charAt(0));
                }
                if (option.get().getArgumentType() == OptionArgumentType.NONE)
                {
                    throw new OptionParseException("option \'" + option.get().getShortForm().get() // NOSONAR
                            + "\' takes no argument");
                }
                final String optionArgument = scrubbedPrefix.substring(1);
                this.parsedOptions.put(option.get(), Optional.ofNullable(optionArgument));
            }

            return false;
        }
    }

    private void registerArgumentHelper(final int context, final String argumentHint,
            final ArgumentArity arity, final ArgumentOptionality optionality)
    {
        if (context < 0)
        {
            throw new CoreException("Context ID must be a positive integer");
        }
        if (this.contextToRegisteredOptionalArgument.getOrDefault(context, false))
        {
            throw new CoreException("Optional argument must be the last registered argument");
        }

        if (arity == ArgumentArity.VARIADIC
                && this.contextToRegisteredVariadicArgument.getOrDefault(context, false))
        {
            throw new CoreException("Cannot register more than one variadic argument");
        }
        if (optionality == ArgumentOptionality.OPTIONAL)
        {
            if (this.contextToRegisteredOptionalArgument.getOrDefault(context, false))
            {
                throw new CoreException("Cannot register more than one optional argument");
            }
            if (this.contextToRegisteredVariadicArgument.getOrDefault(context, false))
            {
                throw new CoreException(
                        "Cannot register both an optional argument and a variadic argument");
            }
            this.contextToRegisteredOptionalArgument.put(context, true);
        }

        if (arity == ArgumentArity.VARIADIC)
        {
            this.contextToRegisteredVariadicArgument.put(context, true);
        }

        final Map<String, ArgumentArity> argumentHintToArity = this.contextToArgumentHintToArity
                .get(context) == null ? new LinkedHashMap<>()
                        : this.contextToArgumentHintToArity.get(context);
        argumentHintToArity.put(argumentHint, arity);
        this.contextToArgumentHintToArity.put(context, argumentHintToArity);

        final Map<String, ArgumentOptionality> argumentHintToOptionality = this.contextToArgumentHintToOptionality
                .get(context) == null ? new LinkedHashMap<>()
                        : this.contextToArgumentHintToOptionality.get(context);
        argumentHintToOptionality.put(argumentHint, optionality);
        this.contextToArgumentHintToOptionality.put(context, argumentHintToOptionality);

        this.registeredContexts.add(context);
    }

    private Optional<SimpleOption> registeredOptionForLongForm(final int context,
            final String longForm) throws AmbiguousAbbreviationException
    {
        return checkForLongOption(longForm, this.contextToRegisteredOptions.get(context), true);
    }

    private Optional<SimpleOption> registeredOptionForShortForm(final int context,
            final Character shortForm)
    {
        return checkForShortOption(shortForm, this.contextToRegisteredOptions.get(context));
    }

    private void registerOptionHelper(final int context, final String longForm,
            final Character shortForm, final String description,
            final OptionOptionality optionality, final OptionArgumentType type,
            final String argumentHint)
    {
        if (context <= 0)
        {
            throw new CoreException("Context ID must be a positive integer (>= 1)");
        }
        final Set<SimpleOption> registeredOptionsForContext = this.contextToRegisteredOptions
                .get(context) == null ? new HashSet<>()
                        : this.contextToRegisteredOptions.get(context);
        registeredOptionsForContext.add(new SimpleOption(longForm, shortForm, description,
                optionality, type, argumentHint));
        this.contextToRegisteredOptions.put(context, registeredOptionsForContext);

        this.registeredContexts.add(context);
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

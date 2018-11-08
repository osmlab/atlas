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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple option and argument parser. Designed specifically to impose constraints on the
 * format of the options. Nothing about this class is thread safe, should you decide to parse in one
 * thread and read results in another.
 * <p>
 * Supports long and short options:<br>
 * --long-option : a long option<br>
 * --long-option-arg=my_argument : a long option with an argument<br>
 * -a : a short option<br>
 * -ab : multiple short options at once<br>
 * <br>
 * Short options cannot have arguments. Additionally, supports argument parsing with unary and
 * variadic arguments.
 * </p>
 *
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
    class SimpleOption
    {
        private final String longForm;
        private final Optional<Character> shortForm;
        private final OptionArgumentType argumentType;
        private final String description;
        private final Optional<String> argumentHint;

        public SimpleOption(final String longForm, final Character shortForm,
                final OptionArgumentType argumentType, final String description)
        {
            this(longForm, shortForm, argumentType, description, null);
        }

        public SimpleOption(final String longForm, final Character shortForm,
                final OptionArgumentType argumentType, final String description,
                final String argumentHint)
        {
            if (longForm == null || longForm.isEmpty())
            {
                throw new CoreException("Long option form cannot be null or empty");
            }
            if (shortForm != null)
            {
                if (!Character.isLetterOrDigit(shortForm))
                {
                    throw new CoreException("Invalid short option {}: must be letter or digit",
                            shortForm);
                }
            }
            if (description == null || description.isEmpty())
            {
                throw new CoreException("Description cannot be null or empty");
            }

            this.longForm = longForm;
            this.shortForm = Optional.ofNullable(shortForm);
            this.argumentType = argumentType;
            this.description = description;
            if (argumentHint != null && !argumentHint.isEmpty())
            {
                this.argumentHint = Optional.of(argumentHint);
            }
            else
            {
                this.argumentHint = Optional.empty();
            }
        }

        public SimpleOption(final String longForm, final OptionArgumentType argumentType,
                final String description)
        {
            this(longForm, null, argumentType, description, null);
        }

        public SimpleOption(final String longForm, final OptionArgumentType argumentType,
                final String description, final String argumentHint)
        {
            this(longForm, null, argumentType, description, argumentHint);
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

    private static final String LONG_FORM_PREFIX = "--";
    private static final String SHORT_FORM_PREFIX = "-";
    private static final String OPTION_ARGUMENT_DELIMITER = "=";
    private static final Logger logger = LoggerFactory
            .getLogger(SimpleOptionAndArgumentParser.class);

    private static final String DEFAULT_LONG_HELP = LONG_FORM_PREFIX + "help";
    private static final String DEFAULT_SHORT_HELP = SHORT_FORM_PREFIX + "h";
    private static final Object DEFAULT_LONG_VERSION = LONG_FORM_PREFIX + "version";;
    private static final Object DEFAULT_SHORT_VERSION = SHORT_FORM_PREFIX + "V";

    private final Set<SimpleOption> registeredOptions;
    private final Map<String, ArgumentArity> registeredArgumentHintToArity;
    private final Map<String, ArgumentOptionality> registeredArgumentHintToType;

    private final Set<String> longFormsSeen;
    private final Set<Character> shortFormsSeen;
    private final Set<String> argumentHintsSeen;

    private boolean registeredVariadicArgument;
    private boolean registeredOptionalArgument;
    private boolean previouslyRegisteredArgumentWasVariadic;

    private final Map<SimpleOption, Optional<String>> parsedOptions;
    private final Map<String, List<String>> parsedArguments;
    private boolean parseStepRan;

    public SimpleOptionAndArgumentParser()
    {
        this.registeredOptions = new LinkedHashSet<>();
        this.registeredArgumentHintToArity = new LinkedHashMap<>();
        this.registeredArgumentHintToType = new LinkedHashMap<>();

        this.longFormsSeen = new HashSet<>();
        this.shortFormsSeen = new HashSet<>();
        this.argumentHintsSeen = new HashSet<>();

        this.registeredVariadicArgument = false;
        this.registeredOptionalArgument = false;
        this.previouslyRegisteredArgumentWasVariadic = false;

        this.parsedOptions = new LinkedHashMap<>();
        this.parsedArguments = new LinkedHashMap<>();
        this.parseStepRan = false;
    }

    /**
     * Get the argument of a given long option, if present.
     *
     * @param longForm
     *            the long form of the option
     * @return an {@link Optional} wrapping the argument
     * @throws CoreException
     *             if longForm does not refer to a registered option
     */
    public Optional<String> getLongOptionArgument(final String longForm)
    {
        if (!this.parseStepRan)
        {
            throw new CoreException("Cannot get results before parsing!");
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
     * Given a hint registered as a unary argument, return the argument value associated with that
     * hint.
     *
     * @param hint
     *            the hint to check
     * @return the value
     * @throws CoreException
     *             if the argument hint was not registered or is not unary
     */
    public String getUnaryArgument(final String hint)
    {
        if (!this.parseStepRan)
        {
            throw new CoreException("Cannot get results before parsing!");
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
            return arguments.get(0);
        }

        throw new CoreException("Critical failure. If you see this, it\'s a bug!");
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
            throw new CoreException("Cannot get results before parsing!");
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

        throw new CoreException("Critical failure. If you see this, it\'s a bug!");
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
            throw new CoreException("Cannot get results before parsing!");
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
     */
    public void parseOptionsAndArguments(final List<String> allArguments)
            throws UnknownOptionException, OptionParseException, ArgumentException
    {
        final List<String> regularArguments = new ArrayList<>();
        boolean seenEndOptionSentinel = false;
        this.parsedArguments.clear();
        this.parsedOptions.clear();
        int regularArgumentCounter = 0;

        for (final String argument : allArguments)
        {
            // Four cases:
            // Argument is "--" -> stop parsing arguments as options
            // Argument starts with "--" -> long form option
            // Argument starts with "-" -> short form option
            // Anything else -> regular argument
            if ("--".equals(argument))
            {
                seenEndOptionSentinel = true;
            }
            else if (argument.startsWith(LONG_FORM_PREFIX) && !seenEndOptionSentinel)
            {
                parseLongFormOption(argument);
            }
            else if (argument.startsWith(SHORT_FORM_PREFIX) && !seenEndOptionSentinel)
            {
                if (SHORT_FORM_PREFIX.equals(argument))
                {
                    regularArguments.add(argument);
                }
                else
                {
                    parseShortFormOption(argument);
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
     * argument value(s) later. Additionally, documentation can use the hint to specify what the
     * argument should be for.
     *
     * @param argumentHint
     *            the hint for the argument
     * @param arity
     *            the argument arity
     * @param type
     *            whether the argument is optional or required
     */
    public void registerArgument(final String argumentHint, final ArgumentArity arity,
            final ArgumentOptionality type)
    {
        throwIfArgumentHintSeen(argumentHint);

        if (argumentHint == null || argumentHint.isEmpty())
        {
            throw new CoreException("Argument hint cannot be null or empty");
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
            this.registeredVariadicArgument = true;
        }
        if (type == ArgumentOptionality.OPTIONAL)
        {
            if (this.registeredOptionalArgument)
            {
                throw new CoreException("Cannot register more than one optional argument");
            }
            if (this.previouslyRegisteredArgumentWasVariadic)
            {
                throw new CoreException(
                        "Cannot register an optional argument after a variadic one");
            }
            this.registeredOptionalArgument = true;
        }

        this.registeredArgumentHintToArity.put(argumentHint, arity);
        this.registeredArgumentHintToType.put(argumentHint, type);

        if (arity == ArgumentArity.VARIADIC)
        {
            this.previouslyRegisteredArgumentWasVariadic = true;
        }
        else
        {
            this.previouslyRegisteredArgumentWasVariadic = false;
        }
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
        this.registeredOptions
                .add(new SimpleOption(longForm, shortForm, OptionArgumentType.NONE, description));
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
    public void registerOption(final String longForm, final String description)
    {
        if (longForm != null)
        {
            throwIfDuplicateLongForm(longForm);
            this.longFormsSeen.add(longForm);
        }
        this.registeredOptions
                .add(new SimpleOption(longForm, OptionArgumentType.NONE, description));
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

    private void parseLongFormOption(final String argument)
            throws UnknownOptionException, OptionParseException
    {
        final String scrubPrefix = argument.substring(2);
        final String[] split = scrubPrefix.split(OPTION_ARGUMENT_DELIMITER, 2);

        final String optionName = split[0];
        final String optionArgument = split.length > 1 ? split[1] : null;
        final Optional<SimpleOption> option = registeredOptionForLongForm(optionName);
        if (option.isPresent())
        {
            if (option.get().getArgumentType() == OptionArgumentType.NONE && optionArgument != null)
            {
                throw new OptionParseException(
                        "option \'" + option.get().getLongForm() + "\' takes no value");
            }
            if (option.get().getArgumentType() == OptionArgumentType.REQUIRED
                    && optionArgument == null)
            {
                throw new OptionParseException(
                        "option \'" + option.get().getLongForm() + "\' needs an argument");
            }
            logger.debug("parsed long option {}={}", option.get().getLongForm(),
                    Optional.ofNullable(optionArgument));
            this.parsedOptions.put(option.get(), Optional.ofNullable(optionArgument));
        }
        else
        {
            throw new UnknownOptionException(optionName);
        }
    }

    private int parseRegularArgument(final String argument, final int regularArgumentSize,
            int regularArgumentCounter) throws ArgumentException
    {
        if (regularArgumentCounter >= this.registeredArgumentHintToArity.size())
        {
            throw new ArgumentException("too many arguments");
        }

        final String argumentHint = (String) this.registeredArgumentHintToArity.keySet()
                .toArray()[regularArgumentCounter];
        final ArgumentArity currentArity = this.registeredArgumentHintToArity.get(argumentHint);
        switch (currentArity)
        {
            case UNARY:
                logger.debug("parsed unary argument hint => {} : value => {}", argumentHint,
                        argument);
                this.parsedArguments.put(argumentHint, Arrays.asList(argument));
                regularArgumentCounter++;
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
                // Case 1 -> [SINGLE...] MULTIPLE
                if (regularArgumentCounter == this.registeredArgumentHintToArity.size() - 1)
                {
                    // do nothing, we can consume the rest of the arguments
                }
                // Case 2 -> [SINGLE...] MULTIPLE SINGLE [SINGLE...]
                else
                {
                    // cutoff point, be sure to save arguments for consumption by subsequent hints
                    if (multiArgumentList.size() == regularArgumentSize
                            - this.registeredArgumentHintToArity.size() + 1)
                    {
                        regularArgumentCounter++;
                        break;
                    }
                }
                break;
            default:
                throw new CoreException("Unrecognized ArgumentArity {}", currentArity);
        }
        return regularArgumentCounter;
    }

    private void parseShortFormOption(final String argument) throws UnknownOptionException
    {
        final String scrubPrefix = argument.substring(1);

        final String optionName = scrubPrefix;

        for (int i = 0; i < optionName.length(); i++)
        {
            final Optional<SimpleOption> option = registeredOptionForShortForm(
                    optionName.charAt(i));
            if (option.isPresent())
            {
                logger.debug("parsed short option {}", option.get().getShortForm().get());
                this.parsedOptions.put(option.get(), Optional.empty());
            }
            else
            {
                throw new UnknownOptionException(optionName);
            }
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

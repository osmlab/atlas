package org.openstreetmap.atlas.utilities.command;

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
 * format of the options.
 *
 * @author lcram
 */
public class SimpleOptionAndArgumentParser
{
    /**
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

    /*
     * TODO: An alternate approach to explore would be: extend the Apache Commons CLI library to
     * impose the necessary constraints without having to reinvent the wheel.
     */

    /**
     * @author lcram
     */
    public enum ArgumentParity
    {
        SINGLE,
        MULTIPLE
    }

    /**
     * @author lcram
     */
    public enum OptionArgumentType
    {
        NONE,
        OPTIONAL,
        REQUIRED
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

    private final Set<SimpleOption> registeredOptions;
    private final List<String> argumentHints;
    private final List<ArgumentParity> argumentParities;
    private final Set<String> longFormsSeen;
    private final Set<Character> shortFormsSeen;
    private final Set<String> argumentHintsSeen;
    private boolean seenMultiParity;

    private final Map<SimpleOption, Optional<String>> parsedOptions;
    private final Map<String, List<String>> parsedArguments;

    private int argumentParityCounter = 0;

    public SimpleOptionAndArgumentParser()
    {
        this.registeredOptions = new LinkedHashSet<>();
        this.argumentHints = new ArrayList<>();
        this.argumentParities = new ArrayList<>();
        this.longFormsSeen = new HashSet<>();
        this.shortFormsSeen = new HashSet<>();
        this.argumentHintsSeen = new HashSet<>();
        this.seenMultiParity = false;

        this.parsedOptions = new LinkedHashMap<>();
        this.parsedArguments = new LinkedHashMap<>();
    }

    public void debugPrint()
    {
        logger.warn("{}", this.registeredOptions);
        logger.warn("{}", this.parsedOptions);
    }

    /**
     * Given a hint defined by a call to
     * {@link SimpleOptionAndArgumentParser#registerArgument(String, ArgumentParity)}, return the
     * argument values associated with that hint.
     *
     * @param hint
     *            the hint to check
     * @return a list of the values
     */
    public List<String> getArgumentForHint(final String hint)
    {
        if (!this.argumentHints.contains(hint))
        {
            throw new CoreException("hint \'{}\' does not correspond to a registered argument",
                    hint);
        }
        final List<String> arguments = this.parsedArguments.get(hint);
        if (arguments != null)
        {
            return arguments;
        }

        throw new CoreException("Critical failure. If you see this, it\'s a bug!");
    }

    public Optional<String> getLongOptionArgument(final String longForm)
    {
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

    public boolean hasOption(final String longForm)
    {
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

    public boolean hasShortOption(final Character shortForm)
    {
        if (!registeredOptionForShortForm(shortForm).isPresent())
        {
            throw new CoreException("{} not a registered option", shortForm);
        }
        final Optional<SimpleOption> option = getParsedOptionFromShortForm(shortForm);
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
            throws UnknownOptionException, OptionParseException
    {
        final List<String> regularArguments = new ArrayList<>();
        boolean seenEndOptionSentinel = false;
        this.parsedArguments.clear();
        this.parsedOptions.clear();
        this.argumentParityCounter = 0;

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

        if (regularArguments.size() < this.argumentHints.size())
        {
            throw new OptionParseException("missing required argument(s)");
        }

        // Now handle the regular arguments
        for (final String regularArgument : regularArguments)
        {
            parseRegularArgument(regularArgument, regularArguments.size());
        }
    }

    public void registerArgument(final String argumentHint, final ArgumentParity parity)
    {
        throwIfArgumentHintSeen(argumentHint);
        if (argumentHint == null || argumentHint.isEmpty())
        {
            throw new CoreException("Argument hint cannot be null or empty");
        }
        if (parity == ArgumentParity.MULTIPLE)
        {
            if (this.seenMultiParity)
            {
                throw new CoreException("Cannot register more than one multiple parity argument");
            }
            else
            {
                this.seenMultiParity = true;
            }
        }
        this.argumentHints.add(argumentHint);
        this.argumentParities.add(parity);
    }

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

    private Optional<SimpleOption> getParsedOptionFromShortForm(final Character shortForm)
    {
        return checkForShortOption(shortForm, this.parsedOptions.keySet());
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
            this.parsedOptions.put(option.get(), Optional.ofNullable(optionArgument));
        }
        else
        {
            throw new UnknownOptionException(optionName);
        }
    }

    private void parseRegularArgument(final String argument, final int regularArgumentSize)
            throws OptionParseException
    {
        if (this.argumentParityCounter >= this.argumentParities.size())
        {
            throw new OptionParseException("too many arguments");
        }
        final ArgumentParity currentParity = this.argumentParities.get(this.argumentParityCounter);
        final String argumentHint = this.argumentHints.get(this.argumentParityCounter);
        switch (currentParity)
        {
            case SINGLE:
                this.parsedArguments.put(argumentHint, Arrays.asList(argument));
                this.argumentParityCounter++;
                break;
            case MULTIPLE:
                List<String> multiArgumentList = this.parsedArguments.get(argumentHint);
                multiArgumentList = multiArgumentList == null ? new ArrayList<>()
                        : multiArgumentList;
                multiArgumentList.add(argument);
                this.parsedArguments.put(argumentHint, multiArgumentList);

                // Two cases:
                // Case 1 -> [SINGLE...] MULTIPLE
                if (this.argumentParityCounter == this.argumentParities.size() - 1)
                {
                    // do nothing, we can consume the rest of the arguments
                }
                // Case 2 -> [SINGLE...] MULTIPLE SINGLE [SINGLE...]
                else
                {
                    // cutoff point, be sure to save arguments for consumption by subsequent hints
                    if (multiArgumentList.size() == regularArgumentSize - this.argumentHints.size()
                            + 1)
                    {
                        this.argumentParityCounter++;
                        break;
                    }
                }
                break;
            default:
                throw new CoreException("Unrecognized ArgumentParity {}", currentParity);
        }

    }

    private void parseShortFormOption(final String argument) throws UnknownOptionException
    {
        final String scrubPrefix = argument.substring(1);

        final String optionName = scrubPrefix;
        if (optionName.length() > 1)
        {
            throw new UnknownOptionException(optionName);
        }

        final Optional<SimpleOption> option = registeredOptionForShortForm(optionName.charAt(0));
        if (option.isPresent())
        {
            this.parsedOptions.put(option.get(), Optional.empty());
        }
        else
        {
            throw new UnknownOptionException(optionName);
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
            throw new CoreException("Cannot register {} hint more than once!", hint);
        }
    }

    private void throwIfDuplicateLongForm(final String longForm)
    {
        if (this.longFormsSeen.contains(longForm))
        {
            throw new CoreException("Cannot register {} more than once!", longForm);
        }
    }

    private void throwIfDuplicateShortForm(final Character shortForm)
    {
        if (this.shortFormsSeen.contains(shortForm))
        {
            throw new CoreException("Cannot register {} more than once!", shortForm);
        }
    }
}

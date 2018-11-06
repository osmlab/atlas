package org.openstreetmap.atlas.utilities.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * A very simple option and argument parser. Designed specifically to impose constraints on the
 * format of the options.
 *
 * @author lcram
 */
public class SimpleOptionAndArgumentParser
{
    /*
     * TODO: An alternate approach to explore would be: extend the Apache Commons CLI library to
     * impose the necessary constraints without having to reinvent the wheel.
     */

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
    }

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
            if (argument.startsWith(LONG_FORM_PREFIX) && !seenEndOptionSentinel)
            {
                parseLongFormOption(argument);
            }
            else if (argument.startsWith(SHORT_FORM_PREFIX) && !seenEndOptionSentinel)
            {
                parseShortFormOption(argument);
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
        switch (currentParity)
        {
            case SINGLE:
                final List<String> list = new ArrayList<>();
                list.add(argument);
                this.parsedArguments.put(argument, list);
                this.argumentParityCounter++;
                break;
            case MULTIPLE:
                final String argumentHint = this.argumentHints.get(this.argumentParityCounter);
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
                // Case 2 -> MULTIPLE SINGLE [SINGLE...]
                else if (this.argumentParityCounter == 0)
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
        for (final SimpleOption option : this.registeredOptions)
        {
            if (option.getLongForm().equals(longForm))
            {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    private Optional<SimpleOption> registeredOptionForShortForm(final Character shortForm)
    {
        for (final SimpleOption option : this.registeredOptions)
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

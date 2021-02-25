package org.openstreetmap.atlas.utilities.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.EnhancedCollectors;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run a Command
 *
 * @author matthieun
 * @author tony
 */
public abstract class Command
{
    /**
     * A command line flag switch. Example -v
     */
    public static class Flag extends Switch<Boolean>
    {
        public Flag(final String name, final String description)
        {
            super(name, description, value -> Boolean.valueOf(value), Optionality.OPTIONAL,
                    Boolean.FALSE.toString());
        }

        public Flag(final String name, final String description, final Optionality optionality)
        {
            super(name, description, value -> Boolean.valueOf(value), optionality,
                    Boolean.FALSE.toString());
        }

        public Flag(final String name, final String description, final Optionality optionality,
                final String defaultValue)
        {
            super(name, description, value -> Boolean.valueOf(value), optionality, defaultValue);
        }
    }

    /**
     * Optionality for Switch
     *
     * @author tony
     */
    public enum Optionality
    {
        REQUIRED,
        OPTIONAL
    }

    /**
     * A command line switch. Example: -file=~/blah.txt
     *
     * @param <T>
     *            The type returned by the Switch
     * @author matthieun
     */
    public static class Switch<T>
    {
        private final String name;
        private final String description;
        private final StringConverter<T> converter;
        private final Optionality optionality;
        private final String defaultResult;

        public Switch(final String name, final String description,
                final StringConverter<T> converter)
        {
            this(name, description, converter, Optionality.OPTIONAL);
        }

        public Switch(final String name, final String description,
                final StringConverter<T> converter, final Optionality optionality)
        {
            this.name = name;
            this.description = description;
            this.converter = converter;
            this.optionality = optionality;
            this.defaultResult = null;
        }

        public Switch(final String name, final String description,
                final StringConverter<T> converter, final Optionality optionality,
                final String defaultResult)
        {
            this.name = name;
            this.description = description;
            this.converter = converter;
            this.optionality = optionality;
            this.defaultResult = defaultResult;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof Switch)
            {
                final Switch<?> that = (Switch<?>) other;
                return this.getName().equals(that.getName())
                        && this.getDescription().equals(that.getDescription());
            }
            return false;
        }

        public T get(final String value)
        {
            if (value == null)
            {
                return null;
            }
            return this.converter.convert(value);
        }

        public T getDefault()
        {
            return get(this.defaultResult);
        }

        public String getDescription()
        {
            String defaultDescription = "";
            if (this.defaultResult != null)
            {
                defaultDescription = " Default is: " + this.defaultResult.toString();
            }
            return this.description + defaultDescription;
        }

        public String getName()
        {
            return this.name;
        }

        public Optionality getOptionality()
        {
            return this.optionality;
        }

        @Override
        public int hashCode()
        {
            return this.name.hashCode() + this.description.hashCode() + this.converter.hashCode();
        }

        @Override
        public String toString()
        {
            return "[Switch: name = " + this.getName() + ", description = " + this.getDescription()
                    + "]";
        }

        protected String getDefaultResult()
        {
            return this.defaultResult;
        }

        protected boolean hasDefaultResult()
        {
            return this.defaultResult != null;
        }
    }

    /**
     * {@link ArrayList} of {@link Switch}es
     *
     * @author matthieun
     */
    public static class SwitchList extends ArrayList<Switch<?>>
    {
        private static final long serialVersionUID = 6310935016772212513L;

        public SwitchList with(final Switch<?>... switches)
        {
            Iterables.asList(switches).forEach(aSwitch -> add(aSwitch));
            return this;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Command.class);

    private CommandMap command = null;
    private Map<Switch<?>, String> lastRawCommand;

    public CommandMap getCommandMap(final String[] arguments)
    {
        if (this.command == null)
        {
            final Map<String, Switch<?>> switches = switches().stream()
                    .collect(EnhancedCollectors.toLinkedMap(Switch::getName, Function.identity()));
            final Map<Switch<?>, String> inputValues = new HashMap<>();
            final Map<String, String> unknownValues = new HashMap<>();
            for (int i = 0; i < arguments.length; i++)
            {
                final String currentSwitch = arguments[i];
                if (currentSwitch.startsWith("-"))
                {
                    try
                    {
                        final String switchString = currentSwitch.substring(1);
                        final StringList switchStringParts = StringList.split(switchString, "=", 2);
                        final String switchName = switchStringParts.get(0);
                        // If this switch is a flag, we have special handling
                        final Switch<?> foundSwitch = switches.get(switchName);
                        if (foundSwitch instanceof Flag)
                        {
                            logger.info("Found flag {} with value {}", switchName, Boolean.TRUE);
                            inputValues.put(foundSwitch, Boolean.TRUE.toString());
                        }
                        else
                        {
                            // just exclude switch if split size not equal to 2, it means empty
                            // string basically. It the switch is required it will fail down the
                            // line, but if it is optional it will basically just ignore the empty
                            // string
                            if (switchStringParts.size() != 2)
                            {
                                logger.warn("Switch [{}] contains empty string", switchString);
                            }
                            else
                            {
                                final String switchValue = switchStringParts.get(1);
                                if (foundSwitch != null)
                                {
                                    inputValues.put(foundSwitch, switchValue);
                                    logger.info("Parsing switch {} -> {}", switchName, switchValue);
                                }
                                else
                                {
                                    unknownValues.put(switchName, switchValue);
                                    logger.warn("Unknown switch {} -> {}", switchName, switchValue);
                                }
                            }
                        }
                    }
                    catch (final Exception e)
                    {
                        throw new CoreException("Problem parsing switch {}", currentSwitch, e);
                    }
                }
            }

            for (final Switch<?> switchObject : switches.values())
            {
                if (!inputValues.containsKey(switchObject))
                {
                    if (switchObject.getOptionality() == Optionality.REQUIRED)
                    {
                        throw new CoreException("Missing Required Switch: {}", switchObject);
                    }
                    if (switchObject.getDefault() != null)
                    {
                        inputValues.put(switchObject, switchObject.getDefaultResult());
                    }
                    logger.warn("Running without switch {}", switchObject);
                }
            }

            this.command = new CommandMap();
            for (final Switch<?> switchObject : inputValues.keySet())
            {
                this.command.put(switchObject.getName(),
                        switchObject.get(inputValues.get(switchObject)));
            }
            this.lastRawCommand = inputValues;
            this.command.putAll(unknownValues);
        }
        return this.command;
    }

    public void run(final String... arguments)
    {
        try
        {
            System.exit(execute(arguments));
        }
        catch (final Throwable throwable)
        {
            logger.error("Command execution failed.", throwable);
            System.exit(1);
        }
    }

    public void runWithoutQuitting(final String... arguments)
    {
        try
        {
            execute(arguments);
        }
        catch (final Throwable throwable)
        {
            throw new CoreException("Command execution failed.", throwable);
        }
    }

    protected String commandSummary()
    {
        final StringList list = new StringList();
        this.command.forEach((key, value) ->
        {
            final StringBuilder builder = new StringBuilder();
            builder.append(key);
            builder.append(" -> ");
            builder.append(value);
            list.add(builder.toString());
        });
        return list.join("\n");
    }

    protected String lastRawCommand(final Switch<?> sswitch)
    {
        return this.lastRawCommand.get(sswitch);
    }

    /**
     * Run the command
     *
     * @param command
     *            The map of Switch name to converted object from the command line
     * @return the exit status to return to the caller
     */
    protected abstract int onRun(CommandMap command);

    /**
     * @return The list of expected switches on the command line
     */
    protected abstract SwitchList switches();

    private int execute(final String[] arguments)
    {
        return onRun(getCommandMap(arguments));
    }
}

package org.openstreetmap.atlas.utilities.command.abstractcommand;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;

/**
 * @author lcram
 */
public class OptionAndArgumentDelegate
{
    private final AbstractAtlasShellToolsCommand parentCommand;

    public OptionAndArgumentDelegate(final AbstractAtlasShellToolsCommand parentCommand)
    {
        this.parentCommand = parentCommand;
    }

    /**
     * Get all registered contexts for this command.
     *
     * @return the set of registered contexts
     */
    public SortedSet<Integer> getFilteredRegisteredContexts()
    {
        return this.parentCommand.getFilteredRegisteredContexts();
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
        return this.parentCommand.getOptionArgument(longForm);
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
        return this.parentCommand.getOptionArgument(longForm, converter);
    }

    /**
     * Get the current context ID of the command's option parser.
     *
     * @return the context ID
     */
    public int getParserContext()
    {
        return this.parentCommand.getParserContext();
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
    public Optional<String> getUnaryArgument(final String hint)
    {
        return this.parentCommand.getUnaryArgument(hint);
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
        return this.parentCommand.getVariadicArgument(hint);
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
    public boolean hasOption(final String longForm)
    {
        return this.parentCommand.hasOption(longForm);
    }

    /**
     * Check if the user supplied the '--verbose' or '-v' option. This is a default option inherited
     * by all commands.
     *
     * @return if --verbose was set
     */
    public boolean hasVerboseOption()
    {
        return this.parentCommand.hasVerboseOption();
    }
}

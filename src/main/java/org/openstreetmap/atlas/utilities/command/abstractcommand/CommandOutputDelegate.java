package org.openstreetmap.atlas.utilities.command.abstractcommand;

import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;
import org.openstreetmap.atlas.utilities.command.terminal.TTYStringBuilder;

/**
 * @author lcram
 */
public class CommandOutputDelegate
{
    private final AbstractAtlasShellToolsCommand parentCommand;

    public CommandOutputDelegate(final AbstractAtlasShellToolsCommand parentCommand)
    {
        this.parentCommand = parentCommand;
    }

    /**
     * Get a {@link TTYStringBuilder} with the correct formatting settings for stderr.
     * Implementations of {@link AbstractAtlasShellToolsCommand} should use this method instead of
     * instantiating their own string builders.
     *
     * @return the string builder
     */
    public TTYStringBuilder getTTYStringBuilderForStderr()
    {
        return this.parentCommand.getTTYStringBuilderForStderr();
    }

    /**
     * Get a {@link TTYStringBuilder} with the correct formatting settings for stdout.
     * Implementations of {@link AbstractAtlasShellToolsCommand} should use this method instead of
     * instantiating their own string builders.
     *
     * @return the string builder
     */
    public TTYStringBuilder getTTYStringBuilderForStdout()
    {
        return this.parentCommand.getTTYStringBuilderForStdout();
    }

    /**
     * Print a message (with no ending newline) to STDERR with the supplied attributes.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    public void printStderr(final String string, final TTYAttribute... attributes)
    {
        this.parentCommand.printStderr(string, attributes);
    }

    /**
     * Print a message (with no ending newline) to STDOUT with the supplied attributes.
     *
     * @param string
     *            the string to print
     * @param attributes
     *            the attributes
     */
    public void printStdout(final String string, final TTYAttribute... attributes)
    {
        this.parentCommand.printStdout(string, attributes);
    }

    /**
     * Prints the supplied message like "commandName: message" to stderr. Automatically appends a
     * newline to the output.
     *
     * @param message
     *            the message
     */
    public void printlnCommandMessage(final String message)
    {
        this.parentCommand.printlnCommandMessage(message);
    }

    /**
     * Prints the supplied message like "commandName: error: message" with automatic coloring to
     * stderr. Automatically appends a newline to the output.
     *
     * @param message
     *            the error message
     */
    public void printlnErrorMessage(final String message)
    {
        this.parentCommand.printlnErrorMessage(message);
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
    public void printlnStderr(final String string, final TTYAttribute... attributes)
    {
        this.parentCommand.printlnStderr(string, attributes);
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
    public void printlnStdout(final String string, final TTYAttribute... attributes)
    {
        this.parentCommand.printlnStdout(string, attributes);
    }

    /**
     * Prints the supplied message like "commandName: warn: message" with automatic coloring to
     * stderr. Automatically appends a newline to the output.
     *
     * @param message
     *            the warn message
     */
    public void printlnWarnMessage(final String message)
    {
        this.parentCommand.printlnWarnMessage(message);
    }
}

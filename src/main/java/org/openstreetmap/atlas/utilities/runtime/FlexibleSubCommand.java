package org.openstreetmap.atlas.utilities.runtime;

import java.io.PrintStream;

import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;

/**
 * All subcommands implement this interface
 *
 * @author cstaylor
 */
public interface FlexibleSubCommand
{
    /**
     * Subcommands will override this method to do whatever they want (display metadata, join
     * multiple atlas files, etc...)
     *
     * @param map
     *            the command line parameters passed to the reader command
     * @return the exit status returned to the operating system
     */
    int execute(CommandMap map);

    /**
     * Explains what this subcommand does
     *
     * @return the text description of the command
     */
    String getDescription();

    /**
     * The name of the command. I suggest you make this all lowercase characters. This will become
     * the first parameter to the command, so it must be unique within the group of all subcommands
     *
     * @return the name of the command
     */
    String getName();

    /**
     * Each subcommand can have its own unique set of command-line switches. If no command-line
     * switches are needed, return an empty SwitchList
     *
     * @return the list of switches supported by this subcommand
     */
    SwitchList switches();

    /**
     * Output usage information for this command to the stream
     *
     * @param writer
     *            where we should write the parameter usage for this subcommand
     */
    void usage(PrintStream writer);
}

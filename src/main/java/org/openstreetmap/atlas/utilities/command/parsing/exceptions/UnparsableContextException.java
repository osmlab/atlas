package org.openstreetmap.atlas.utilities.command.parsing.exceptions;

import java.util.SortedSet;

import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * @author lcram
 */
public class UnparsableContextException extends Exception
{
    private static final long serialVersionUID = 8204676424116770097L;

    public UnparsableContextException(final SortedSet<String> exceptionMessagesWeSaw)
    {
        super("could not match command line to a usage context: "
                + System.getProperty("line.separator") + new StringList(exceptionMessagesWeSaw)
                        .join(System.getProperty("line.separator")));
    }
}

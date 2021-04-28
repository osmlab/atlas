package org.openstreetmap.atlas.utilities.command.parsing.exceptions;

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

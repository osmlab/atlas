package org.openstreetmap.atlas.utilities.command.parsing.exceptions;

/**
 * @author lcram
 */
public class UnknownOptionException extends Exception
{
    private static final long serialVersionUID = 8506034533362610699L;

    public UnknownOptionException(final Character option)
    {
        super("unknown short option \'" + option + "\'");
    }

    public UnknownOptionException(final String option)
    {
        super("unknown long option \'" + option + "\'");
    }
}

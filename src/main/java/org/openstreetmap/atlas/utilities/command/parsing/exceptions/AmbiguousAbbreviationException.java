package org.openstreetmap.atlas.utilities.command.parsing.exceptions;

/**
 * @author lcram
 */
public class AmbiguousAbbreviationException extends Exception
{
    private static final long serialVersionUID = 8506034533362610699L;

    public AmbiguousAbbreviationException(final String option, final String ambiguousOptions)
    {
        super("long option \'" + option + "\' is ambiguous (" + ambiguousOptions + ")");
    }
}

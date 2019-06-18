package org.openstreetmap.atlas.geography.atlas.change.exception;

/**
 * @author Yazad Khambata
 */
public class EmptyChangeException extends ChangeException
{
    private static final String MESSAGE = "Change cannot be empty.";

    public EmptyChangeException()
    {
        super(MESSAGE);
    }

    public EmptyChangeException(final Throwable cause)
    {
        super(MESSAGE, cause);
    }
}

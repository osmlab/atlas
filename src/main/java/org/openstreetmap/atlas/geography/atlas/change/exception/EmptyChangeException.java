package org.openstreetmap.atlas.geography.atlas.change.exception;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author Yazad Khambata
 */
public class EmptyChangeException extends CoreException
{
    private static final String MESSAGE = "Change cannot be empty.";

    public EmptyChangeException()
    {
        super(messageWithToken(MESSAGE));
    }

    public EmptyChangeException(final Throwable cause)
    {
        super(messageWithToken(MESSAGE), cause);
    }
}

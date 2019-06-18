package org.openstreetmap.atlas.geography.atlas.change.exception;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author Yazad Khambata
 */
public class ChangeException extends CoreException
{
    public ChangeException(final String message)
    {
        super(messageWithToken(message));
    }

    public ChangeException(final String message, final Object... arguments)
    {
        super(messageWithToken(message), arguments);
    }

    public ChangeException(final String message, final Throwable cause)
    {
        super(messageWithToken(message), cause);
    }

    public ChangeException(final String message, final Throwable cause, final Object... arguments)
    {
        super(messageWithToken(message), cause, arguments);
    }
}

package org.openstreetmap.atlas.geography.atlas.exception;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * @author matthieun
 */
public class AtlasIntegrityException extends CoreException
{
    private static final long serialVersionUID = -2780280960455310936L;

    public AtlasIntegrityException(final String message)
    {
        super(message);
    }

    public AtlasIntegrityException(final String message, final Object... arguments)
    {
        super(message, arguments);
    }

    public AtlasIntegrityException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public AtlasIntegrityException(final String message, final Throwable cause,
            final Object... arguments)
    {
        super(message, cause, arguments);
    }
}

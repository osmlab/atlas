package org.openstreetmap.atlas.utilities.http.rest;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Thrown when an HTTP Request returns a status code we don't like
 *
 * @author cstaylor
 */
public class DislikedResponseCodeException extends CoreException
{
    private static final long serialVersionUID = -172869039301922865L;

    private final int statusCode;

    public DislikedResponseCodeException(final int statusCode, final String message,
            final Object... arguments)
    {
        super(message, arguments);
        this.statusCode = statusCode;
    }

    public int getStatusCode()
    {
        return this.statusCode;
    }
}

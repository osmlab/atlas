package org.openstreetmap.atlas.exception;

import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Thrown when there's a problem loading an atlas from a packed atlas so we can quickly collect all
 * of the missing or damaged files.
 *
 * @author cstaylor
 */
public class LoadAtlasFromResourceException extends CoreException
{
    private static final long serialVersionUID = 65439602944966080L;

    private final transient Resource resource;

    public LoadAtlasFromResourceException(final Resource resource, final String message)
    {
        super(message);
        this.resource = resource;
    }

    public LoadAtlasFromResourceException(final Resource resource, final String message,
            final Object... arguments)
    {
        super(message, arguments);
        this.resource = resource;
    }

    public LoadAtlasFromResourceException(final Resource resource, final String message,
            final Throwable cause)
    {
        super(message, cause);
        this.resource = resource;
    }

    public LoadAtlasFromResourceException(final Resource resource, final String message,
            final Throwable cause, final Object... arguments)
    {
        super(message, cause, arguments);
        this.resource = resource;
    }

    public Resource getResource()
    {
        return this.resource;
    }
}

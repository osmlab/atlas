package org.openstreetmap.atlas.streaming.resource;

import java.io.InputStream;
import java.io.OutputStream;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Writable resource from an {@link OutputStream}
 *
 * @author matthieun
 */
public class OutputStreamWritableResource extends AbstractWritableResource
{
    private final OutputStream out;

    public OutputStreamWritableResource(final OutputStream out)
    {
        this.out = out;
    }

    @Override
    protected InputStream onRead()
    {
        throw new CoreException("This resource cannot be read.");
    }

    @Override
    protected OutputStream onWrite()
    {
        return this.out;
    }
}

package org.openstreetmap.atlas.streaming;

import java.io.Closeable;
import java.io.Flushable;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Stream utility
 *
 * @author matthieun
 */
public final class Streams
{
    /**
     * Safe close of a {@link Closeable} item.
     *
     * @param stream
     *            The stream to close.
     */
    public static void close(final Closeable stream)
    {
        if (stream == null)
        {
            return;
        }
        try
        {
            stream.close();
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not close stream", e);
        }
    }

    /**
     * Safe flush of a {@link Flushable} item.
     *
     * @param stream
     *            The stream to flush.
     */
    public static void flush(final Flushable stream)
    {
        if (stream == null)
        {
            return;
        }
        try
        {
            stream.flush();
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not flush stream", e);
        }
    }

    private Streams()
    {
    }
}
